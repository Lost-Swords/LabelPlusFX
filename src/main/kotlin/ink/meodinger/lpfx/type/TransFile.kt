package ink.meodinger.lpfx.type

import ink.meodinger.lpfx.util.ReLazy
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.sortByDigit
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import javafx.beans.InvalidationListener
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import java.io.File


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A MEO Translation file
 */
@JsonIncludeProperties("version", "comment", "groupList", "transMap")
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.ANY)
open class TransFile @JsonCreator constructor(
    @JsonProperty("version")   version:   IntArray                                    = DEFAULT_VERSION,
    @JsonProperty("comment")   comment:   String                                      = DEFAULT_COMMENT,
    @JsonProperty("groupList") groupList: MutableList<TransGroup>                     = ArrayList(),
    @JsonProperty("transMap")  transMap:  MutableMap<String, MutableList<TransLabel>> = HashMap()
) {

    companion object {

        val DEFAULT_VERSION = intArrayOf(1, 0)

        const val DEFAULT_COMMENT = "使用 LabelPlusFX 导出"
        val DEFAULT_COMMENT_LIST = arrayListOf(
            DEFAULT_COMMENT,
            "Default Comment\nYou can edit me",
            "由 MoeFlow.com 导出",
            "由MoeTra.com导出"
        )

        val DEFAULT_TRANS_FILE = TransFile()

        object LPTransFile {
            const val PIC_START   = ">>>>>>>>["
            const val PIC_END     = "]<<<<<<<<"
            const val LABEL_START = "----------------["
            const val LABEL_END   = "]----------------"
            const val PROP_START  = "["
            const val PROP_END    = "]"
            const val SPLIT       = ","
            const val SEPARATOR   = "-"

            val DEFAULT_COLOR_HEX_LIST = listOf(
                "FF0000", "0000FF", "008000",
                "1E90FF", "FFD700", "FF00FF",
                "A0522D", "FF4500", "9400D3"
            )
        }

    }

    // ----- Exception ----- //

    class TransFileException(message: String) : RuntimeException(message) {
        companion object {
            fun pictureNotFound(picName: String) =
                TransFileException(String.format(I18N["exception.trans_file.picture_not_found.s"], picName))
            fun pictureStillInUse(picName: String) =
                TransFileException(String.format(I18N["exception.trans_file.picture_in_use.s"], picName))

            fun transGroupNameRepeated(groupName: String) =
                TransFileException(String.format(I18N["exception.trans_file.group_name_repeated.s"], groupName))
            fun transGroupIdNegative(groupId: Int) =
                TransFileException(String.format(I18N["exception.trans_file.group_id_negative.i"], groupId))
            fun transGroupIdOutOfBounds(groupId: Int) =
                TransFileException(String.format(I18N["exception.trans_file.group_id_out_of_bounds.i"], groupId))
            fun transGroupNotFound(groupName: String) =
                TransFileException(String.format(I18N["exception.trans_file.group_not_found.s"], groupName))

            fun transLabelIndexRepeated(picName: String, index: Int) =
                TransFileException(String.format(I18N["exception.trans_file.label_index_repeated.is"], index, picName))
            fun transLabelGroupIdOutOfBounds(groupId: Int) =
                TransFileException(String.format(I18N["exception.trans_file.label_groupId_out_of_bounds.i"], groupId))
            fun transLabelNotFound(picName: String, index: Int) =
                TransFileException(String.format(I18N["exception.trans_file.label_not_found.is"], index, picName))
        }
    }

    // ----- Project Files Management ----- //

    lateinit var projectFolder: File

    private val fileMap = HashMap<String, File>()
    fun getFile(picName: String): File {
        if (!transMapObservable.keys.contains(picName)) throw TransFileException.pictureNotFound(picName)
        return fileMap[picName] ?: projectFolder.resolve(picName).also { setFile(picName, it) }
    }
    fun setFile(picName: String, file: File) {
        if (!transMapObservable.keys.contains(picName)) throw TransFileException.pictureNotFound(picName)
        fileMap[picName] = file
    }
    fun removeFile(picName: String) {
        if (transMapObservable.keys.contains(picName)) throw TransFileException.pictureStillInUse(picName)
        fileMap.remove(picName)
    }
    fun checkLost(): List<String> {
        val lost = ArrayList<String>()
        picNames.forEach { picture -> if (!getFile(picture).exists()) lost.add(picture) }
        return lost
    }

    // ----- Properties ----- //

    val versionProperty: ReadOnlyObjectProperty<IntArray> = SimpleObjectProperty(version)
    val commentProperty: StringProperty = SimpleStringProperty(comment)
    val groupListProperty: ListProperty<TransGroup> = SimpleListProperty(FXCollections.observableArrayList(groupList))
    val transMapProperty: MapProperty<String, ObservableList<TransLabel>> = SimpleMapProperty(FXCollections.observableMap(transMap.mapValues { FXCollections.observableArrayList(it.value) }))

    // ----- Lazy ---- //

    private val sortedPicNamesLazy: ReLazy<List<String>> = ReLazy { sortByDigit(transMapObservable.keys.toList()) } // copy
    val sortedPicNames: List<String> by sortedPicNamesLazy

    // ----- Accessible Fields ----- //

    val version: IntArray by versionProperty
    var comment: String by commentProperty
    val groupListObservable: ObservableList<TransGroup> by groupListProperty
    val transMapObservable: ObservableMap<String, ObservableList<TransLabel>> by transMapProperty

    val groupCount: Int get() = groupListObservable.size
    val groupNames: List<String> get() = List(groupListObservable.size) { groupListObservable[it].name }
    val groupColors: List<String> get() = List(groupListObservable.size) { groupListObservable[it].colorHex }

    /// NOTE: sortedPicNames is slow, find a way to make it faster (maybe use ReLazy)
    val picCount: Int get() = transMapObservable.size
    val picNames: List<String> get() = transMapObservable.keys.toList() // copy

    // ----- JSON Getters ----- //

    @Suppress("unused") protected val groupList: List<TransGroup> by groupListObservable
    @Suppress("unused") protected val transMap: Map<String, List<TransLabel>> by transMapObservable

    // ----- Init ----- //

    init {
        transMapObservable.addListener(InvalidationListener { sortedPicNamesLazy.refresh() })
    }

    // ----- TransGroup ----- //

    fun getGroupIdByName(name: String): Int {
        groupListObservable.forEachIndexed { index, transGroup -> if (transGroup.name == name) return index }

        throw TransFileException.transGroupNotFound(name)
    }

    fun addTransGroup(transGroup: TransGroup) {
        for (group in groupListObservable)
            if (group.name == transGroup.name)
                throw TransFileException.transGroupNameRepeated(transGroup.name)

        groupListObservable.add(transGroup)
    }
    fun getTransGroup(groupId: Int): TransGroup {
        if (groupId < 0) throw TransFileException.transGroupIdNegative(groupId)
        if (groupId >= groupListObservable.size) throw TransFileException.transGroupIdOutOfBounds(groupId)

        return groupListObservable[groupId]
    }
    fun removeTransGroup(groupId: Int) {
        if (groupId < 0) throw TransFileException.transGroupIdNegative(groupId)
        if (groupId >= groupListObservable.size) throw TransFileException.transGroupIdOutOfBounds(groupId)

        groupListObservable.removeAt(groupId)
    }

    fun isGroupUnused(groupId: Int): Boolean {
        for (list in transMapObservable.values) for (label in list) if (label.groupId == groupId) return false
        return true
    }

    // ----- TransList (TransMap) ----- //

    open fun addTransList(picName: String) {
        transMapObservable[picName] = FXCollections.observableArrayList()
    }
    open fun getTransList(picName: String): List<TransLabel> {
        return transMapObservable[picName] ?: throw TransFileException.pictureNotFound(picName)
    }
    open fun removeTransList(picName: String) {
        if (transMapObservable[picName] != null) transMapObservable.remove(picName)
        else throw TransFileException.pictureNotFound(picName)
    }

    // ----- TransLabel ----- //

    fun addTransLabel(picName: String, transLabel: TransLabel) {
        val list = transMapObservable[picName] ?: throw TransFileException.pictureNotFound(picName)

        val (index, groupId) = transLabel
        if (groupId >= groupListObservable.size) throw TransFileException.transLabelGroupIdOutOfBounds(groupId)
        for (label in list) if (label.index == index) throw TransFileException.transLabelIndexRepeated(picName, index)

        list.add(transLabel)
    }
    fun getTransLabel(picName: String, labelIndex: Int): TransLabel {
        for (label in transMapObservable[picName] ?: throw TransFileException.pictureNotFound(picName))
            if (label.index == labelIndex)
                return label

        throw TransFileException.transLabelNotFound(picName, labelIndex)
    }
    fun removeTransLabel(picName: String, labelIndex: Int) {
        val list = transMapObservable[picName] ?: throw TransFileException.pictureNotFound(picName)
        var toRemove: TransLabel? = null
        for (label in list) if (label.index == labelIndex) toRemove = label

        if (toRemove != null) list.remove(toRemove)
        else throw TransFileException.transLabelNotFound(picName, labelIndex)
    }

    // ----- Other ----- //

    fun clone(): TransFile {
        val version = this.version.clone()
        val comment = this.comment
        val groupList = MutableList(groupListObservable.size) { groupListObservable[it].clone() }
        val transMap = LinkedHashMap<String, MutableList<TransLabel>>().apply {
            putAll(sortedPicNames.map { it to transMapObservable[it]!!.sorted { l1, l2 -> l1.index - l2.index } })
        }

        return TransFile(version, comment, groupList, transMap)
    }

    fun toJsonString(): String {
        return ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .writeValueAsString(this)
    }

    override fun toString(): String = ObjectMapper().writeValueAsString(this)

}

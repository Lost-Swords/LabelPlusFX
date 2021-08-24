package info.meodinger.lpfx

import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

/**
 * Author: Meodinger
 * Date: 2021/8/1
 * Location: info.meodinger.lpfx
 */
const val WIDTH = 900.0
const val HEIGHT = 600.0
const val NOT_FOUND = -1

enum class WorkMode { LabelMode, InputMode }
val DefaultWorkMode = WorkMode.InputMode

enum class ViewMode { IndexMode, GroupMode }
val DefaultViewMode = ViewMode.GroupMode

enum class FileType { LPFile, MeoFile }
fun isMeoFile(filePath: String): Boolean {
    return filePath.endsWith(EXTENSION_MEO)
}
fun isLPFile(filePath: String): Boolean {
    return filePath.endsWith(EXTENSION_LP)
}
fun getFileType(path: String): FileType {
    if (isMeoFile(path)) return FileType.MeoFile
    if (isLPFile(path)) return FileType.LPFile
    throw IllegalArgumentException(I18N["exception.illegal_argument.invalid_file_extension"])
}

val EXTENSIONS_PIC = listOf(".png", ".jpg", ".jpeg")
const val EXTENSION_MEO = ".json"
const val EXTENSION_LP = ".txt"
const val EXTENSION_PACK = ".zip"
const val EXTENSION_BAK = ".bak"
const val FOLDER_NAME_BAK = "bak"

const val AUTO_SAVE_DELAY = 5 * 60 * 1000L
const val AUTO_SAVE_PERIOD = 3 * 60 * 1000L
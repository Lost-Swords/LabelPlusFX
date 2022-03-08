package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.using

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.collections.ArrayList


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * Abstract properties structure for save/load/check
 */
abstract class AbstractProperties(val name: String) {

    companion object {

        private const val KV_SPILT = "="
        private const val COMMENT_HEAD = "#"

        @Throws(IOException::class)
        fun load(path: Path, instance: AbstractProperties) {
            try {
                val lines = Files.newBufferedReader(path).readLines()
                for (line in lines) {
                    if (line.isBlank()) continue
                    if (line.trim().startsWith(COMMENT_HEAD)) continue

                    val prop = line.split(KV_SPILT, limit = 2)
                    if (prop.size == 2) {
                        if (instance.tryGet(prop[0]) != null) instance[prop[0]].set(prop[1]) else continue
                    }
                }
            } catch (e: IndexOutOfBoundsException) {
                throw IOException("Load properties failed: KV format invalid").initCause(e)
            } catch (e: IOException) {
                throw IOException("Save properties I/O failed").initCause(e)
            }
        }

        @Throws(IOException::class)
        fun save(path: Path, instance: AbstractProperties) {
            using {
                val writer = Files.newBufferedWriter(path).autoClose()
                for (property in instance.properties) {
                    writer.write(
                        StringBuilder()
                            .append(property.key)
                            .append(KV_SPILT)
                            .append(property.value)
                            .append("\n")
                            .toString()
                    )
                }
            } catch { e: Exception ->
                throw IOException("Save properties I/O failed").initCause(e)
            }
        }

        /**
         * Be careful!
         */
        fun getPropertiesOf(properties: AbstractProperties): List<CProperty> = properties.properties
    }

    protected abstract val default: List<CProperty>
    protected val properties = ArrayList<CProperty>()

    @Throws(IOException::class, NumberFormatException::class)
    abstract fun load()
    @Throws(IOException::class)
    abstract fun save()

    fun useDefault() {
        properties.clear()
        properties.addAll(default.map(CProperty::copy))
    }

    private fun tryGet(key: String): CProperty? {
        for (property in properties) if (property.key == key) return property
        return null
    }

    operator fun get(key: String): CProperty = tryGet(key) ?: throw IllegalArgumentException(I18N["exception.property.property_not_found.k"])

}

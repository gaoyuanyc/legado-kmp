package io.legado.shared.constant

object BookType {
    const val video = 0b100
    const val text = 0b1000
    const val updateError = 0b10000
    const val audio = 0b100000
    const val image = 0b1000000
    const val webFile = 0b10000000
    const val local = 0b100000000
    const val archive = 0b1000000000
    const val notShelf = 0b100_0000_0000

    const val allBookType = video or text or image or audio or webFile
    const val allBookTypeLocal = video or text or image or audio or webFile or local
    const val localTag = "loc_book"
    const val webDavTag = "webDav::"
}

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.security.CryptoManager
import java.io.Serializable

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val encryptedTitle: String,
    val titleIv: String,
    val encryptedContent: String,
    val contentIv: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val colorHex: String = "#FF1E1E22", // Default dark slate card shade
    val isPinned: Boolean = false,
    val isSensitive: Boolean = false
) {
    fun toDecryptedNote(): DecryptedNote {
        val title = CryptoManager.decrypt(encryptedTitle, titleIv)
        val content = CryptoManager.decrypt(encryptedContent, contentIv)
        return DecryptedNote(
            id = id,
            title = if (title == "[Decryption Error]") "" else title,
            content = if (content == "[Decryption Error]") "" else content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            colorHex = colorHex,
            isPinned = isPinned,
            isSensitive = isSensitive
        )
    }
}

data class DecryptedNote(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val colorHex: String = "#FF1E1E22",
    val isPinned: Boolean = false,
    val isSensitive: Boolean = false
) : Serializable {
    fun toEncryptedNote(): Note {
        val encryptedTitleData = CryptoManager.encrypt(title)
        val encryptedContentData = CryptoManager.encrypt(content)
        return Note(
            id = id,
            encryptedTitle = encryptedTitleData.ciphertext,
            titleIv = encryptedTitleData.iv,
            encryptedContent = encryptedContentData.ciphertext,
            contentIv = encryptedContentData.iv,
            createdAt = createdAt,
            updatedAt = updatedAt,
            colorHex = colorHex,
            isPinned = isPinned,
            isSensitive = isSensitive
        )
    }
}

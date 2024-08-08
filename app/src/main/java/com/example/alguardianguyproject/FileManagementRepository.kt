package com.example.alguardianguyproject

import java.io.File

class FileManagementRepository {
    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        if (file.exists()) {

            if (file.delete()) {
                println("File deleted successfully")
                return true
            }
            println("Failed to delete file")
            return false
        }

        println("File does not exist")
        return false
    }
}
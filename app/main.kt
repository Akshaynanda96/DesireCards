package com.example.desirecards

import java.sql.DriverManager

fun main() {
    val url = "jdbc:postgresql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/test"
    val username = "2cJUT8WcYZchzMD.root"
    val password = "0848of0dYLouxteG"

    try {
        val connection = DriverManager.getConnection(url, username, password)

        if (connection.isValid(5)) {
            println("✅ Database Connected Successfully")
        } else {
            println("❌ Database Not Connected")
        }

        connection.close()

    } catch (e: Exception) {
        println("❌ Connection Failed")
        e.printStackTrace()
    }
}

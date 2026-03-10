package com.yoesuv.mynote

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MyNotesApplication

fun main(args: Array<String>) {
	runApplication<MyNotesApplication>(*args)
}

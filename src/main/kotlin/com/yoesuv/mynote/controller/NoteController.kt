package com.yoesuv.mynote.controller

import com.yoesuv.mynote.dto.note.NoteRequest
import com.yoesuv.mynote.dto.note.NoteResponse
import com.yoesuv.mynote.service.NoteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notes")
class NoteController(
    private val noteService: NoteService
) {

    @GetMapping
    fun getAllNotes(@RequestParam(required = false) categoryId: Long?): ResponseEntity<List<NoteResponse>> {
        val notes = noteService.getAllNotes(categoryId)
        return ResponseEntity.ok(notes)
    }

    @GetMapping("/{id}")
    fun getNoteById(@PathVariable id: Long): ResponseEntity<NoteResponse> {
        val note = noteService.getNoteById(id)
        return ResponseEntity.ok(note)
    }

    @PostMapping
    fun createNote(@Valid @RequestBody request: NoteRequest): ResponseEntity<NoteResponse> {
        val note = noteService.createNote(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(note)
    }

    @PutMapping("/{id}")
    fun updateNote(
        @PathVariable id: Long,
        @Valid @RequestBody request: NoteRequest
    ): ResponseEntity<NoteResponse> {
        val note = noteService.updateNote(id, request)
        return ResponseEntity.ok(note)
    }

    @DeleteMapping("/{id}")
    fun deleteNote(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        noteService.deleteNote(id)
        return ResponseEntity.ok(mapOf("message" to "Note deleted successfully"))
    }
}
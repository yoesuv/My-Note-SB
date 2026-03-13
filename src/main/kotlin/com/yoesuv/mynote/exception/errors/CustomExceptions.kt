package com.yoesuv.mynote.exception.errors

class EntityNotFoundException(entityName: String, id: Any) :
    RuntimeException("$entityName not found with id: $id")

class UserAlreadyExistsException(email: String) :
    RuntimeException("User already exists with email: $email")

class InvalidCredentialsException(message: String = "Invalid email or password") :
    RuntimeException(message)

class UnauthorizedException(message: String = "Unauthorized access") :
    RuntimeException(message)
package services

import org.mindrot.jbcrypt.BCrypt

class BcryptPasswordHasher {
    fun checkPassword(plainPassword: String, hashedPassword: String) = BCrypt.checkpw(plainPassword, hashedPassword)
    fun hashPassword(plainPassword: String) = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12))
}

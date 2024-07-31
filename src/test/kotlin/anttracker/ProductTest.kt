/* ProductTest.kt
Revision History:
Rev. 1 - 2024/7/1 Original by Eitan Barylko
-------------------------------------------------------------------------------
This file contains the tests for the function
validateProductName.
-------------------------------------------------------------------------------
 */
import anttracker.product.validateProductName
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * The goal of this test is to confirm that validateProductName returns false on
 * empty names and names longer than thirty characters
 * This test is a boundary normal/stress test
 */
class ProductTest :
    DescribeSpec({
        xdescribe("validateProductName") {
            describe("when name is blank") {
                it("returns false") {
                    // Given
                    val name = ""
                    // When
                    val actual = validateProductName(name)
                    // Then
                    actual shouldBe false
                }
            }
            describe(
                "When name length is within 1 to 30 characters and there exists no " +
                    "other user with the same name",
            ) {
                describe("When the name is four characters long") {
                    it("Returns true") {
                        // Given
                        val name = "Cain"
                        // When
                        val actual = validateProductName(name)
                        // Then
                        actual shouldBe true
                    }
                }
                describe("When the name is one character long") {
                    it("Returns true") {
                        // Given
                        val name = "C"
                        // When
                        val actual = validateProductName(name)
                        // Then
                        actual shouldBe true
                    }
                }
                describe("When the name is 30 characters long") {
                    it("Returns true") {
                        // Given
                        val name = "012345678901234567890123456789"
                        // When
                        val actual = validateProductName(name)
                        // Then
                        actual shouldBe true
                    }
                }
            }
            describe("When name length is longer than 30 characters") {
                it("Returns false") {
                    // Given
                    val name = "0123456789012345678901234567890"
                    // When
                    val actual = validateProductName(name)
                    // Then
                    actual shouldBe false
                }
            }
            describe(
                "When name length is within 1-30 characters but " +
                    "an existing user has the same name",
            ) {
                it("Returns false") {
                    // Given
                    val name = "John"
                    // When
                    val actual = validateProductName(name)
                    // Then
                    actual shouldBe false
                }
            }
        }
    })

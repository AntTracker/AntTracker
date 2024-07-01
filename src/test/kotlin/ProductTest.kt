import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ProductTest :
    DescribeSpec({
        describe("validateProductName") {
            describe("when name is empty") {
                it("returns false") {
                    // Given
                    val name = ""
                    // When
                    val actual = validateProductName(name)
                    // Then
                    actual shouldBe false
                }
            }
            describe("When name length is within 1 to 30 characters") {
                it("Returns true") {
                    // Given
                    val name = "Cain"
                    // When
                    val actual = validateProductName(name)
                    // Then
                    actual shouldBe true
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
        }
    })

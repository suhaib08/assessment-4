import java.util.*;

public class LoanProcessingQA {
    
    // Test results tracking
    static class TestResult {
        String testName;
        boolean passed;
        String message;
        
        TestResult(String testName, boolean passed, String message) {
            this.testName = testName;
            this.passed = passed;
            this.message = message;
        }
    }
    
    static List<TestResult> results = new ArrayList<>();
    
    // Test counter
    static int testCount = 0;
    static int passCount = 0;
    static int failCount = 0;
    
    // Helper method to assert
    static void assertTrue(String testName, boolean condition, String message) {
        testCount++;
        if (condition) {
            passCount++;
            results.add(new TestResult(testName, true, "PASS: " + message));
            System.out.println("✓ PASS: " + testName + " - " + message);
        } else {
            failCount++;
            results.add(new TestResult(testName, false, "FAIL: " + message));
            System.out.println("✗ FAIL: " + testName + " - " + message);
        }
    }
    
    static void assertFalse(String testName, boolean condition, String message) {
        assertTrue(testName, !condition, message);
    }
    
    static void assertEquals(String testName, double expected, double actual, double delta, String message) {
        testCount++;
        boolean passed = Math.abs(expected - actual) <= delta;
        if (passed) {
            passCount++;
            results.add(new TestResult(testName, true, "PASS: " + message));
            System.out.println("✓ PASS: " + testName + " - " + message + " (expected: " + expected + ", actual: " + actual + ")");
        } else {
            failCount++;
            results.add(new TestResult(testName, false, "FAIL: " + message));
            System.out.println("✗ FAIL: " + testName + " - " + message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }
    
    // Test 1: Minimum Age Boundary
    static void testMinimumAge() {
        System.out.println("\n--- Test 1: Minimum Age Boundary ---");
        
        // Age below minimum (should fail)
        LoanProcessingSystem.LoanApplicant belowMin = new LoanProcessingSystem.LoanApplicant(
            "TEST001", 20, 20000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        LoanProcessingSystem.LoanDecision decision = LoanProcessingSystem.processApplication(belowMin);
        assertFalse("Min Age - Below Minimum", decision.approved, "Age 20 should be rejected (below min 21)");
        
        // Age at minimum (should pass)
        LoanProcessingSystem.LoanApplicant atMin = new LoanProcessingSystem.LoanApplicant(
            "TEST002", 21, 20000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(atMin);
        assertTrue("Min Age - At Minimum", decision.approved, "Age 21 should be approved");
    }
    
    // Test 2: Maximum Age Boundary
    static void testMaximumAge() {
        System.out.println("\n--- Test 2: Maximum Age Boundary ---");
        
        // Age at maximum (should pass)
        LoanProcessingSystem.LoanApplicant atMax = new LoanProcessingSystem.LoanApplicant(
            "TEST003", 65, 20000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        LoanProcessingSystem.LoanDecision decision = LoanProcessingSystem.processApplication(atMax);
        assertTrue("Max Age - At Maximum", decision.approved, "Age 65 should be approved");
        
        // Age above maximum (should fail)
        LoanProcessingSystem.LoanApplicant aboveMax = new LoanProcessingSystem.LoanApplicant(
            "TEST004", 66, 20000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(aboveMax);
        assertFalse("Max Age - Above Maximum", decision.approved, "Age 66 should be rejected (above max 65)");
    }
    
    // Test 3: Invalid Salary
    static void testInvalidSalary() {
        System.out.println("\n--- Test 3: Invalid Salary ---");
        
        // Zero salary (should fail)
        LoanProcessingSystem.LoanApplicant zeroSalary = new LoanProcessingSystem.LoanApplicant(
            "TEST005", 35, 0, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        LoanProcessingSystem.LoanDecision decision = LoanProcessingSystem.processApplication(zeroSalary);
        assertFalse("Invalid Salary - Zero", decision.approved, "Zero salary should be rejected");
        
        // Below minimum salary (should fail)
        LoanProcessingSystem.LoanApplicant belowMinSalary = new LoanProcessingSystem.LoanApplicant(
            "TEST006", 35, 5000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(belowMinSalary);
        assertFalse("Invalid Salary - Below Minimum", decision.approved, "Salary below 10000 should be rejected");
        
        // Minimum salary (should pass credit checks)
        LoanProcessingSystem.LoanApplicant minSalary = new LoanProcessingSystem.LoanApplicant(
            "TEST007", 35, 10000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 10000, 60
        );
        decision = LoanProcessingSystem.processApplication(minSalary);
        assertTrue("Invalid Salary - At Minimum", decision.approved, "Salary at 10000 should be accepted");
    }
    
    // Test 4: Poor Credit Score
    static void testPoorCreditScore() {
        System.out.println("\n--- Test 4: Poor Credit Score ---");
        
        // Very poor credit score (should fail)
        LoanProcessingSystem.LoanApplicant poorCredit = new LoanProcessingSystem.LoanApplicant(
            "TEST008", 35, 20000, 0, 450, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        LoanProcessingSystem.LoanDecision decision = LoanProcessingSystem.processApplication(poorCredit);
        assertFalse("Poor Credit - Score 450", decision.approved, "Credit score below 500 should be rejected");
        
        // Score at threshold (should pass)
        LoanProcessingSystem.LoanApplicant thresholdCredit = new LoanProcessingSystem.LoanApplicant(
            "TEST009", 35, 20000, 0, 500, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(thresholdCredit);
        assertTrue("Poor Credit - Score 500", decision.approved, "Credit score at 500 should be acceptable");
        
        // Invalid credit score range
        LoanProcessingSystem.LoanApplicant invalidCredit = new LoanProcessingSystem.LoanApplicant(
            "TEST010", 35, 20000, 0, 1001, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(invalidCredit);
        assertFalse("Poor Credit - Out of Range", !decision.reason.contains("Invalid input"), "Score > 1000 should be rejected");
    }
    
    // Test 5: Existing Loan Exceeding Threshold
    static void testExistingLoanThreshold() {
        System.out.println("\n--- Test 5: Existing Loan Exceeding Threshold ---");
        
        // High existing loan affecting DTI
        LoanProcessingSystem.LoanApplicant highExistingLoan = new LoanProcessingSystem.LoanApplicant(
            "TEST011", 35, 20000, 50000, 750, LoanProcessingSystem.EmploymentType.SALARIED, 100000, 60
        );
        LoanProcessingSystem.LoanDecision decision = LoanProcessingSystem.processApplication(highExistingLoan);
        assertTrue("Existing Loan - High Amount", decision.dti > 0, "DTI calculated with existing loan");
        
        // Zero existing loan
        LoanProcessingSystem.LoanApplicant noExistingLoan = new LoanProcessingSystem.LoanApplicant(
            "TEST012", 35, 20000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 100000, 60
        );
        decision = LoanProcessingSystem.processApplication(noExistingLoan);
        assertTrue("Existing Loan - Zero", decision.approved, "Zero existing loan should allow approval");
    }
    
    // Test 6: High Debt-to-Income Ratio
    static void testHighDTI() {
        System.out.println("\n--- Test 6: High Debt-to-Income Ratio ---");
        
        // DTI exceeding maximum (should fail)
        LoanProcessingSystem.LoanApplicant highDTI = new LoanProcessingSystem.LoanApplicant(
            "TEST013", 35, 10000, 50000, 750, LoanProcessingSystem.EmploymentType.SALARIED, 100000, 60
        );
        LoanProcessingSystem.LoanDecision decision = LoanProcessingSystem.processApplication(highDTI);
        assertFalse("High DTI - Exceeds Maximum", decision.approved, "DTI > 50% should be rejected");
        assertTrue("High DTI - DTI Value", decision.dti > 0.5, "DTI should exceed 0.5 (50%)");
        
        // Low DTI (should pass)
        LoanProcessingSystem.LoanApplicant lowDTI = new LoanProcessingSystem.LoanApplicant(
            "TEST014", 35, 50000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(lowDTI);
        assertTrue("High DTI - Low DTI Accepted", decision.approved, "Low DTI should be accepted");
        assertTrue("High DTI - DTI Low Value", decision.dti < 0.5, "DTI should be below 0.5 (50%)");
    }
    
    // Test 7: Different Employment Categories
    static void testEmploymentCategories() {
        System.out.println("\n--- Test 7: Different Employment Categories ---");
        
        // SALARIED
        LoanProcessingSystem.LoanApplicant salaried = new LoanProcessingSystem.LoanApplicant(
            "TEST015", 35, 20000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        LoanProcessingSystem.LoanDecision decision = LoanProcessingSystem.processApplication(salaried);
        assertTrue("Employment - Salaried", decision.approved, "Salaried should be approved");
        assertEquals("Employment - Salaried Rate", 7.5, decision.interestRateAnnual, 0.1, "Salaried gets -0.5% modifier");
        
        // SELF_EMPLOYED
        LoanProcessingSystem.LoanApplicant selfEmployed = new LoanProcessingSystem.LoanApplicant(
            "TEST016", 35, 20000, 0, 750, LoanProcessingSystem.EmploymentType.SELF_EMPLOYED, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(selfEmployed);
        assertTrue("Employment - Self Employed", decision.approved, "Self-employed should be approved");
        assertEquals("Employment - Self Employed Rate", 9.0, decision.interestRateAnnual, 0.1, "Self-employed gets +1% modifier");
        
        // CONTRACT
        LoanProcessingSystem.LoanApplicant contract = new LoanProcessingSystem.LoanApplicant(
            "TEST017", 35, 20000, 0, 750, LoanProcessingSystem.EmploymentType.CONTRACT, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(contract);
        assertTrue("Employment - Contract", decision.approved, "Contract should be approved");
        assertEquals("Employment - Contract Rate", 8.5, decision.interestRateAnnual, 0.1, "Contract gets +0.5% modifier");
        
        // UNEMPLOYED
        LoanProcessingSystem.LoanApplicant unemployed = new LoanProcessingSystem.LoanApplicant(
            "TEST018", 35, 20000, 0, 750, LoanProcessingSystem.EmploymentType.UNEMPLOYED, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(unemployed);
        assertFalse("Employment - Unemployed", decision.approved, "Unemployed should be rejected");
    }
    
    // Test 8: Boundary Loan Amounts
    static void testBoundaryLoanAmounts() {
        System.out.println("\n--- Test 8: Boundary Loan Amounts ---");
        
        // Very small loan
        LoanProcessingSystem.LoanApplicant smallLoan = new LoanProcessingSystem.LoanApplicant(
            "TEST019", 35, 20000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 1000, 12
        );
        LoanProcessingSystem.LoanDecision decision = LoanProcessingSystem.processApplication(smallLoan);
        assertTrue("Boundary Loan - Small", decision.approved, "Small loan should be approved");
        assertEquals("Boundary Loan - Small Amount", 1000, decision.eligibleAmount, 1, "Eligible amount correct");
        
        // Very large loan
        LoanProcessingSystem.LoanApplicant largeLoan = new LoanProcessingSystem.LoanApplicant(
            "TEST020", 35, 50000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 1000000, 120
        );
        decision = LoanProcessingSystem.processApplication(largeLoan);
        assertTrue("Boundary Loan - Large", decision.eligibleAmount > 0, "Large loan should have eligible amount");
        
        // Zero loan (should fail)
        LoanProcessingSystem.LoanApplicant zeroLoan = new LoanProcessingSystem.LoanApplicant(
            "TEST021", 35, 20000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 0, 60
        );
        decision = LoanProcessingSystem.processApplication(zeroLoan);
        assertFalse("Boundary Loan - Zero", decision.approved, "Zero loan should be rejected");
    }
    
    // Test 9: EMI Calculation Accuracy
    static void testEMICalculation() {
        System.out.println("\n--- Test 9: EMI Calculation Accuracy ---");
        
        // Known EMI calculation
        // Principal: 100000, Rate: 7.5% p.a., Months: 60
        // Monthly Rate: 7.5/100/12 = 0.00625
        // Expected EMI ≈ 1983.86
        
        LoanProcessingSystem.LoanApplicant testApplicant = new LoanProcessingSystem.LoanApplicant(
            "TEST022", 35, 50000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 100000, 60
        );
        LoanProcessingSystem.LoanDecision decision = LoanProcessingSystem.processApplication(testApplicant);
        
        assertTrue("EMI Calculation - Value Positive", decision.emi > 0, "EMI should be positive");
        assertEquals("EMI Calculation - Accuracy", 1983.86, decision.emi, 50, "EMI calculation within margin");
        
        // Test with zero interest rate scenario
        LoanProcessingSystem.LoanApplicant zeroRateLoan = new LoanProcessingSystem.LoanApplicant(
            "TEST023", 35, 50000, 0, 900, LoanProcessingSystem.EmploymentType.SALARIED, 12000, 12
        );
        decision = LoanProcessingSystem.processApplication(zeroRateLoan);
        assertTrue("EMI Calculation - Any Loan", decision.emi >= 0, "EMI should be calculated for any valid loan");
    }
    
    // Test 10: Credit Score to Interest Rate Mapping
    static void testCreditScoreToRateMapping() {
        System.out.println("\n--- Test 10: Credit Score to Interest Rate Mapping ---");
        
        // Credit Score >= 750: base rate 8.0
        LoanProcessingSystem.LoanApplicant excellent = new LoanProcessingSystem.LoanApplicant(
            "TEST024", 35, 20000, 0, 800, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        LoanProcessingSystem.LoanDecision decision = LoanProcessingSystem.processApplication(excellent);
        assertEquals("Rate Mapping - Excellent", 7.5, decision.interestRateAnnual, 0.1, "Credit 800 should give ~7.5% rate");
        
        // Credit Score >= 700 && < 750: base rate 9.0
        LoanProcessingSystem.LoanApplicant good = new LoanProcessingSystem.LoanApplicant(
            "TEST025", 35, 20000, 0, 720, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(good);
        assertEquals("Rate Mapping - Good", 8.5, decision.interestRateAnnual, 0.1, "Credit 720 should give ~8.5% rate");
        
        // Credit Score >= 650 && < 700: base rate 10.5
        LoanProcessingSystem.LoanApplicant fair = new LoanProcessingSystem.LoanApplicant(
            "TEST026", 35, 20000, 0, 680, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(fair);
        assertEquals("Rate Mapping - Fair", 10.0, decision.interestRateAnnual, 0.1, "Credit 680 should give ~10% rate");
    }
    
    // Test 11: Invalid Input Handling - Null Customer ID
    static void testInvalidInputHandling() {
        System.out.println("\n--- Test 11: Invalid Input Handling ---");
        
        // Empty customer ID
        LoanProcessingSystem.LoanApplicant emptyID = new LoanProcessingSystem.LoanApplicant(
            "", 35, 20000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        LoanProcessingSystem.LoanDecision decision = LoanProcessingSystem.processApplication(emptyID);
        assertFalse("Invalid Input - Empty ID", decision.approved, "Empty customer ID should be rejected");
        assertTrue("Invalid Input - Error Message", decision.reason.contains("Invalid input"), "Should contain error message");
        
        // Negative age
        LoanProcessingSystem.LoanApplicant negativeAge = new LoanProcessingSystem.LoanApplicant(
            "TEST027", -5, 20000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(negativeAge);
        assertFalse("Invalid Input - Negative Age", decision.approved, "Negative age should be rejected");
        
        // Negative salary
        LoanProcessingSystem.LoanApplicant negativeSalary = new LoanProcessingSystem.LoanApplicant(
            "TEST028", 35, -5000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(negativeSalary);
        assertFalse("Invalid Input - Negative Salary", decision.approved, "Negative salary should be rejected");
        
        // Negative loan tenure
        LoanProcessingSystem.LoanApplicant negativeTenure = new LoanProcessingSystem.LoanApplicant(
            "TEST029", 35, 20000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, -12
        );
        decision = LoanProcessingSystem.processApplication(negativeTenure);
        assertFalse("Invalid Input - Negative Tenure", decision.approved, "Negative tenure should be rejected");
    }
    
    // Test 12: Exception Handling
    static void testExceptionHandling() {
        System.out.println("\n--- Test 12: Exception Handling ---");
        
        // Negative existing loan
        LoanProcessingSystem.LoanApplicant negativeExisting = new LoanProcessingSystem.LoanApplicant(
            "TEST030", 35, 20000, -1000, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        LoanProcessingSystem.LoanDecision decision = LoanProcessingSystem.processApplication(negativeExisting);
        assertFalse("Exception - Negative Existing Loan", decision.approved, "Negative existing loan should be rejected");
        
        // Very high values
        LoanProcessingSystem.LoanApplicant veryHighValues = new LoanProcessingSystem.LoanApplicant(
            "TEST031", 35, 1000000, 5000000, 750, LoanProcessingSystem.EmploymentType.SALARIED, 10000000, 360
        );
        decision = LoanProcessingSystem.processApplication(veryHighValues);
        assertTrue("Exception - Large Values", decision.reason != null, "Should handle large values without exception");
        
        // Very low monthly salary but high request
        LoanProcessingSystem.LoanApplicant mismatchedAmounts = new LoanProcessingSystem.LoanApplicant(
            "TEST032", 35, 10000, 100000, 750, LoanProcessingSystem.EmploymentType.SALARIED, 1000000, 60
        );
        decision = LoanProcessingSystem.processApplication(mismatchedAmounts);
        assertTrue("Exception - Mismatched Amounts", decision.reason != null, "Should handle mismatched amounts gracefully");
    }
    
    // Test 13: Eligible Amount Calculation
    static void testEligibleAmountCalculation() {
        System.out.println("\n--- Test 13: Eligible Amount Calculation ---");
        
        // Eligible amount should not exceed requested amount
        LoanProcessingSystem.LoanApplicant applicant = new LoanProcessingSystem.LoanApplicant(
            "TEST033", 35, 20000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        LoanProcessingSystem.LoanDecision decision = LoanProcessingSystem.processApplication(applicant);
        assertTrue("Eligible Amount - Not Exceed Requested", decision.eligibleAmount <= applicant.requestedLoanAmount, 
                  "Eligible amount should not exceed requested");
        
        // For high income, eligible amount should be close to requested
        LoanProcessingSystem.LoanApplicant highIncome = new LoanProcessingSystem.LoanApplicant(
            "TEST034", 35, 100000, 0, 750, LoanProcessingSystem.EmploymentType.SALARIED, 50000, 60
        );
        decision = LoanProcessingSystem.processApplication(highIncome);
        assertEquals("Eligible Amount - High Income", 50000, decision.eligibleAmount, 1000, 
                    "High income should have eligible amount close to requested");
    }
    
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("LOAN PROCESSING SYSTEM - QA TEST SUITE");
        System.out.println("================================================================================");
        System.out.println();
        
        // Run all tests
        testMinimumAge();
        testMaximumAge();
        testInvalidSalary();
        testPoorCreditScore();
        testExistingLoanThreshold();
        testHighDTI();
        testEmploymentCategories();
        testBoundaryLoanAmounts();
        testEMICalculation();
        testCreditScoreToRateMapping();
        testInvalidInputHandling();
        testExceptionHandling();
        testEligibleAmountCalculation();
        
        // Print summary
        System.out.println("\n" + "================================================================================");
        System.out.println("QA TEST SUMMARY");
        System.out.println("================================================================================");
        System.out.println("Total Tests: " + testCount);
        System.out.println("Passed: " + passCount + " ✓");
        System.out.println("Failed: " + failCount + " ✗");
        System.out.println("Pass Rate: " + String.format("%.2f", (passCount * 100.0 / testCount)) + "%");
        System.out.println("================================================================================");
        
        if (failCount == 0) {
            System.out.println("✓ ALL TESTS PASSED!");
        } else {
            System.out.println("✗ SOME TESTS FAILED. Review above for details.");
        }
        System.out.println("================================================================================");
    }
}

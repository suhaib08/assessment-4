import java.util.*;

class LoanProcessingSystem {
    
    enum EmploymentType {
        SALARIED, SELF_EMPLOYED, CONTRACT, UNEMPLOYED
    }
    
    static class LoanApplicant {
        String customerId;
        int age;
        double monthlySalary;
        double existingLoanAmount;
        int creditScore;
        EmploymentType employmentType;
        double requestedLoanAmount;
        int loanTenureMonths;
        
        LoanApplicant(String customerId, int age, double monthlySalary, 
                     double existingLoanAmount, int creditScore, 
                     EmploymentType employmentType, double requestedLoanAmount, 
                     int loanTenureMonths) {
            this.customerId = customerId;
            this.age = age;
            this.monthlySalary = monthlySalary;
            this.existingLoanAmount = existingLoanAmount;
            this.creditScore = creditScore;
            this.employmentType = employmentType;
            this.requestedLoanAmount = requestedLoanAmount;
            this.loanTenureMonths = loanTenureMonths;
        }
    }
    
    static class LoanDecision {
        boolean approved;
        double eligibleAmount;
        double interestRateAnnual;
        double emi;
        double dti;
        String reason;
        
        LoanDecision(boolean approved, double eligibleAmount, double interestRateAnnual,
                    double emi, double dti, String reason) {
            this.approved = approved;
            this.eligibleAmount = eligibleAmount;
            this.interestRateAnnual = interestRateAnnual;
            this.emi = emi;
            this.dti = dti;
            this.reason = reason;
        }
    }
    
    // Policy/configurable parameters
    static final int MIN_AGE = 21;
    static final int MAX_AGE = 65;
    static final double MIN_MONTHLY_SALARY = 10000.0;
    static final double MAX_DTI = 0.5;  // 50%
    static final double EXISTING_LOAN_ANNUAL_RATE = 10.0;  // percent
    static final int EXISTING_LOAN_REMAINING_MONTHS = 12;
    
    static double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
    
    static double roundRatio(double x) {
        return Math.round(x * 10000.0) / 10000.0;
    }
    
    static void validateApplicant(LoanApplicant a) throws Exception {
        if (a == null) {
            throw new Exception("Applicant is null");
        }
        if (a.customerId == null || a.customerId.trim().isEmpty()) {
            throw new Exception("Customer ID missing");
        }
        if (a.age <= 0) {
            throw new Exception("Age must be positive");
        }
        if (a.monthlySalary <= 0) {
            throw new Exception("Monthly salary must be positive");
        }
        if (a.existingLoanAmount < 0) {
            throw new Exception("Existing loan amount cannot be negative");
        }
        if (a.creditScore < 0 || a.creditScore > 1000) {
            throw new Exception("Credit score out of realistic range (0-1000)");
        }
        if (a.requestedLoanAmount <= 0) {
            throw new Exception("Requested loan amount must be positive");
        }
        if (a.loanTenureMonths <= 0) {
            throw new Exception("Loan tenure months must be positive");
        }
        if (a.employmentType == null) {
            throw new Exception("Employment type required");
        }
    }
    
    static double calculateEmi(double principal, double annualRatePercent, int months) {
        if (principal <= 0 || months <= 0) {
            return 0.0;
        }
        double monthlyRate = annualRatePercent / 100.0 / 12.0;
        if (monthlyRate == 0) {
            return principal / months;
        }
        double pow_ = Math.pow(1 + monthlyRate, months);
        double emi = principal * monthlyRate * pow_ / (pow_ - 1);
        return emi;
    }
    
    static double calculatePrincipalFromEmi(double emi, double annualRatePercent, int months) {
        if (emi <= 0 || months <= 0) {
            return 0.0;
        }
        double monthlyRate = annualRatePercent / 100.0 / 12.0;
        if (monthlyRate == 0) {
            return emi * months;
        }
        double pow_ = Math.pow(1 + monthlyRate, months);
        double principal = emi * (pow_ - 1) / (monthlyRate * pow_);
        return principal;
    }
    
    static double calculateInterestRate(int creditScore, EmploymentType emp) {
        double baseRate;
        if (creditScore >= 750) {
            baseRate = 8.0;
        } else if (creditScore >= 700) {
            baseRate = 9.0;
        } else if (creditScore >= 650) {
            baseRate = 10.5;
        } else if (creditScore >= 600) {
            baseRate = 12.0;
        } else {
            baseRate = 15.0;
        }
        
        double modifier = 0.0;
        if (emp == EmploymentType.SALARIED) {
            modifier = -0.5;
        } else if (emp == EmploymentType.SELF_EMPLOYED) {
            modifier = 1.0;
        } else if (emp == EmploymentType.CONTRACT) {
            modifier = 0.5;
        } else if (emp == EmploymentType.UNEMPLOYED) {
            modifier = 3.0;
        }
        
        double finalRate = Math.max(0.1, baseRate + modifier);
        return finalRate;
    }
    
    static double getCreditEligibilityFactor(int creditScore) {
        if (creditScore >= 750) {
            return 1.0;
        }
        if (creditScore >= 700) {
            return 0.95;
        }
        if (creditScore >= 650) {
            return 0.9;
        }
        if (creditScore >= 600) {
            return 0.75;
        }
        return 0.5;
    }
    
    static LoanDecision processApplication(LoanApplicant a) {
        try {
            validateApplicant(a);
            
            double existingEmi = calculateEmi(a.existingLoanAmount, 
                                            EXISTING_LOAN_ANNUAL_RATE, 
                                            EXISTING_LOAN_REMAINING_MONTHS);
            
            double rate = calculateInterestRate(a.creditScore, a.employmentType);
            
            double requestedEmi = calculateEmi(a.requestedLoanAmount, rate, a.loanTenureMonths);
            
            double dti = (a.monthlySalary > 0) ? 
                        (existingEmi + requestedEmi) / a.monthlySalary : 
                        Double.POSITIVE_INFINITY;
            
            double maxAllowedEmi = Math.max(0.0, MAX_DTI * a.monthlySalary - existingEmi);
            
            double maxEligiblePrincipal = calculatePrincipalFromEmi(maxAllowedEmi, rate, a.loanTenureMonths);
            
            double creditCap = getCreditEligibilityFactor(a.creditScore);
            maxEligiblePrincipal *= creditCap;
            
            double eligibleAmount = Math.max(0.0, Math.min(a.requestedLoanAmount, maxEligiblePrincipal));
            
            List<String> reasons = new ArrayList<>();
            boolean approved = true;
            
            if (a.age < MIN_AGE || a.age > MAX_AGE) {
                approved = false;
                reasons.add("Age out of bounds.");
            }
            if (a.monthlySalary < MIN_MONTHLY_SALARY) {
                approved = false;
                reasons.add("Monthly salary below minimum required.");
            }
            if (a.employmentType == EmploymentType.UNEMPLOYED) {
                approved = false;
                reasons.add("Applicant is unemployed.");
            }
            if (a.creditScore < 500) {
                approved = false;
                reasons.add("Very poor credit score.");
            }
            if (dti > MAX_DTI) {
                approved = false;
                reasons.add("High debt-to-income ratio.");
            }
            if (eligibleAmount < Math.max(0.01, a.requestedLoanAmount * 0.01)) {
                approved = false;
                reasons.add("Requested amount not eligible based on income/DTI/credit.");
            }
            
            double finalEmi = approved ? calculateEmi(eligibleAmount, rate, a.loanTenureMonths) : 0.0;
            
            if (reasons.isEmpty()) {
                reasons.add("Meets policy checks.");
            }
            
            return new LoanDecision(
                approved,
                round2(eligibleAmount),
                round2(rate),
                round2(finalEmi),
                roundRatio(dti),
                String.join(" ", reasons)
            );
            
        } catch (Exception e) {
            return new LoanDecision(false, 0.0, 0.0, 0.0, 0.0, "Invalid input: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        // Built-in test cases with predefined inputs and outputs
        List<Map<String, Object>> testCases = new ArrayList<>();
        
        testCases.add(createTestCase(
            "C001", 30, 20000, 5000, 750, EmploymentType.SALARIED, 100000, 60,
            "Test Case 1: High credit score, salaried employee"
        ));
        
        testCases.add(createTestCase(
            "C002", 45, 15000, 10000, 650, EmploymentType.SELF_EMPLOYED, 50000, 48,
            "Test Case 2: Medium credit score, self-employed"
        ));
        
        testCases.add(createTestCase(
            "C003", 25, 50000, 0, 800, EmploymentType.SALARIED, 200000, 84,
            "Test Case 3: Young professional, no existing loan"
        ));
        
        testCases.add(createTestCase(
            "C004", 60, 12000, 20000, 600, EmploymentType.CONTRACT, 30000, 36,
            "Test Case 4: Near retirement age, contract worker"
        ));
        
        testCases.add(createTestCase(
            "C005", 35, 8000, 5000, 550, EmploymentType.UNEMPLOYED, 25000, 24,
            "Test Case 5: Low salary, unemployed (should fail)"
        ));
        
        System.out.println("================================================================================");
        System.out.println("LOAN PROCESSING SYSTEM - BUILT-IN TEST CASES");
        System.out.println("================================================================================");
        
        for (int i = 0; i < testCases.size(); i++) {
            Map<String, Object> test = testCases.get(i);
            LoanApplicant applicant = (LoanApplicant) test.get("applicant");
            String description = (String) test.get("description");
            
            System.out.println("\n" + description);
            System.out.println("--------------------------------------------------------------------------------");
            
            LoanDecision decision = processApplication(applicant);
            
            System.out.println("Input:");
            System.out.printf("  Customer ID: %s%n", applicant.customerId);
            System.out.printf("  Age: %d%n", applicant.age);
            System.out.printf("  Monthly Salary: $%,.2f%n", applicant.monthlySalary);
            System.out.printf("  Existing Loan Amount: $%,.2f%n", applicant.existingLoanAmount);
            System.out.printf("  Credit Score: %d%n", applicant.creditScore);
            System.out.printf("  Employment Type: %s%n", applicant.employmentType);
            System.out.printf("  Requested Loan Amount: $%,.2f%n", applicant.requestedLoanAmount);
            System.out.printf("  Loan Tenure: %d months%n", applicant.loanTenureMonths);
            
            System.out.println("\nOutput:");
            System.out.printf("  Approved: %b%n", decision.approved);
            System.out.printf("  Eligible Amount: $%,.2f%n", decision.eligibleAmount);
            System.out.printf("  Annual Interest Rate: %.2f%%%n", decision.interestRateAnnual);
            System.out.printf("  Monthly EMI: $%,.2f%n", decision.emi);
            System.out.printf("  Debt-to-Income Ratio: %.2f%%%n", decision.dti * 100);
            System.out.printf("  Reason: %s%n", decision.reason);
        }
        
        System.out.println("\n" + "================================================================================");
        System.out.println("END OF TEST CASES");
        System.out.println("================================================================================");
    }
    
    static Map<String, Object> createTestCase(String customerId, int age, double salary,
                                              double existing, int score, EmploymentType emp,
                                              double requested, int tenure, String description) {
        Map<String, Object> testCase = new HashMap<>();
        testCase.put("applicant", new LoanApplicant(customerId, age, salary, existing, 
                                                    score, emp, requested, tenure));
        testCase.put("description", description);
        return testCase;
    }
}

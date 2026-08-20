import math
from dataclasses import dataclass
from enum import Enum
from typing import Optional


class EmploymentType(Enum):
    SALARIED = "SALARIED"
    SELF_EMPLOYED = "SELF_EMPLOYED"
    CONTRACT = "CONTRACT"
    UNEMPLOYED = "UNEMPLOYED"


@dataclass
class LoanApplicant:
    customer_id: str
    age: int
    monthly_salary: float
    existing_loan_amount: float
    credit_score: int
    employment_type: EmploymentType
    requested_loan_amount: float
    loan_tenure_months: int


@dataclass
class LoanDecision:
    approved: bool
    eligible_amount: float
    interest_rate_annual: float  # percent
    emi: float
    dti: float
    reason: str


# Policy/configurable parameters
MIN_AGE = 21
MAX_AGE = 65
MIN_MONTHLY_SALARY = 10_000.0
MAX_DTI = 0.5  # 50%
EXISTING_LOAN_ANNUAL_RATE = 10.0  # assumed annual rate for existing loan (percent)
EXISTING_LOAN_REMAINING_MONTHS = 12  # assumed remaining months for existing loan


def round2(x: float) -> float:
    return round(x * 100.0) / 100.0


def round_ratio(x: float) -> float:
    return round(x * 10000.0) / 10000.0


def validate_applicant(a: LoanApplicant):
    if a is None:
        raise ValueError("Applicant is None")
    if not a.customer_id or not a.customer_id.strip():
        raise ValueError("Customer ID missing")
    if a.age <= 0:
        raise ValueError("Age must be positive")
    if a.monthly_salary <= 0:
        raise ValueError("Monthly salary must be positive")
    if a.existing_loan_amount < 0:
        raise ValueError("Existing loan amount cannot be negative")
    if a.credit_score < 0 or a.credit_score > 1000:
        raise ValueError("Credit score out of realistic range (0-1000)")
    if a.requested_loan_amount <= 0:
        raise ValueError("Requested loan amount must be positive")
    if a.loan_tenure_months <= 0:
        raise ValueError("Loan tenure months must be positive")
    if a.employment_type is None:
        raise ValueError("Employment type required")


def calculate_emi(principal: float, annual_rate_percent: float, months: int) -> float:
    if principal <= 0 or months <= 0:
        return 0.0
    monthly_rate = annual_rate_percent / 100.0 / 12.0
    if monthly_rate == 0:
        return principal / months
    pow_ = math.pow(1 + monthly_rate, months)
    emi = principal * monthly_rate * pow_ / (pow_ - 1)
    return emi


def calculate_principal_from_emi(emi: float, annual_rate_percent: float, months: int) -> float:
    if emi <= 0 or months <= 0:
        return 0.0
    monthly_rate = annual_rate_percent / 100.0 / 12.0
    if monthly_rate == 0:
        return emi * months
    pow_ = math.pow(1 + monthly_rate, months)
    principal = emi * (pow_ - 1) / (monthly_rate * pow_)
    return principal


def calculate_interest_rate(credit_score: int, emp: EmploymentType) -> float:
    if credit_score >= 750:
        base_rate = 8.0
    elif credit_score >= 700:
        base_rate = 9.0
    elif credit_score >= 650:
        base_rate = 10.5
    elif credit_score >= 600:
        base_rate = 12.0
    else:
        base_rate = 15.0

    modifier = 0.0
    if emp == EmploymentType.SALARIED:
        modifier = -0.5
    elif emp == EmploymentType.SELF_EMPLOYED:
        modifier = 1.0
    elif emp == EmploymentType.CONTRACT:
        modifier = 0.5
    elif emp == EmploymentType.UNEMPLOYED:
        modifier = 3.0

    final_rate = max(0.1, base_rate + modifier)
    return final_rate


def get_credit_eligibility_factor(credit_score: int) -> float:
    if credit_score >= 750:
        return 1.0
    if credit_score >= 700:
        return 0.95
    if credit_score >= 650:
        return 0.9
    if credit_score >= 600:
        return 0.75
    return 0.5


def process_application(a: LoanApplicant) -> LoanDecision:
    try:
        validate_applicant(a)

        existing_emi = calculate_emi(a.existing_loan_amount, EXISTING_LOAN_ANNUAL_RATE, EXISTING_LOAN_REMAINING_MONTHS)

        rate = calculate_interest_rate(a.credit_score, a.employment_type)

        requested_emi = calculate_emi(a.requested_loan_amount, rate, a.loan_tenure_months)

        dti = (existing_emi + requested_emi) / a.monthly_salary if a.monthly_salary > 0 else float('inf')

        max_allowed_emi = max(0.0, MAX_DTI * a.monthly_salary - existing_emi)

        max_eligible_principal = calculate_principal_from_emi(max_allowed_emi, rate, a.loan_tenure_months)

        credit_cap = get_credit_eligibility_factor(a.credit_score)
        max_eligible_principal *= credit_cap

        eligible_amount = max(0.0, min(a.requested_loan_amount, max_eligible_principal))

        reasons = []
        approved = True

        if a.age < MIN_AGE or a.age > MAX_AGE:
            approved = False
            reasons.append("Age out of bounds.")
        if a.monthly_salary < MIN_MONTHLY_SALARY:
            approved = False
            reasons.append("Monthly salary below minimum required.")
        if a.employment_type == EmploymentType.UNEMPLOYED:
            approved = False
            reasons.append("Applicant is unemployed.")
        if a.credit_score < 500:
            approved = False
            reasons.append("Very poor credit score.")
        if dti > MAX_DTI:
            approved = False
            reasons.append("High debt-to-income ratio.")
        if eligible_amount < max(0.01, a.requested_loan_amount * 0.01):
            approved = False
            reasons.append("Requested amount not eligible based on income/DTI/credit.")

        final_emi = calculate_emi(eligible_amount, rate, a.loan_tenure_months) if approved else 0.0

        if not reasons:
            reasons.append("Meets policy checks.")

        return LoanDecision(
            approved=approved,
            eligible_amount=round2(eligible_amount),
            interest_rate_annual=round2(rate),
            emi=round2(final_emi),
            dti=round_ratio(dti),
            reason=" ".join(reasons)
        )
    except ValueError as ve:
        return LoanDecision(False, 0.0, 0.0, 0.0, 0.0, f"Invalid input: {ve}")
    except Exception as e:
        return LoanDecision(False, 0.0, 0.0, 0.0, 0.0, f"Processing error: {e}")


if __name__ == "__main__":
    # Built-in test cases with predefined inputs and outputs
    test_cases = [
        {
            "input": LoanApplicant("C001", 30, 20000, 5000, 750, EmploymentType.SALARIED, 100000, 60),
            "description": "Test Case 1: High credit score, salaried employee"
        },
        {
            "input": LoanApplicant("C002", 45, 15000, 10000, 650, EmploymentType.SELF_EMPLOYED, 50000, 48),
            "description": "Test Case 2: Medium credit score, self-employed"
        },
        {
            "input": LoanApplicant("C003", 25, 50000, 0, 800, EmploymentType.SALARIED, 200000, 84),
            "description": "Test Case 3: Young professional, no existing loan"
        },
        {
            "input": LoanApplicant("C004", 60, 12000, 20000, 600, EmploymentType.CONTRACT, 30000, 36),
            "description": "Test Case 4: Near retirement age, contract worker"
        },
        {
            "input": LoanApplicant("C005", 35, 8000, 5000, 550, EmploymentType.UNEMPLOYED, 25000, 24),
            "description": "Test Case 5: Low salary, unemployed (should fail)"
        },
    ]

    print("=" * 80)
    print("LOAN PROCESSING SYSTEM - BUILT-IN TEST CASES")
    print("=" * 80)

    for i, test in enumerate(test_cases, 1):
        applicant = test["input"]
        print(f"\n{test['description']}")
        print("-" * 80)
        
        decision = process_application(applicant)

        print(f"Input:")
        print(f"  Customer ID: {applicant.customer_id}")
        print(f"  Age: {applicant.age}")
        print(f"  Monthly Salary: ${applicant.monthly_salary:,.2f}")
        print(f"  Existing Loan Amount: ${applicant.existing_loan_amount:,.2f}")
        print(f"  Credit Score: {applicant.credit_score}")
        print(f"  Employment Type: {applicant.employment_type.value}")
        print(f"  Requested Loan Amount: ${applicant.requested_loan_amount:,.2f}")
        print(f"  Loan Tenure: {applicant.loan_tenure_months} months")

        print(f"\nOutput:")
        print(f"  Approved: {decision.approved}")
        print(f"  Eligible Amount: ${decision.eligible_amount:,.2f}")
        print(f"  Annual Interest Rate: {decision.interest_rate_annual:.2f}%")
        print(f"  Monthly EMI: ${decision.emi:,.2f}")
        print(f"  Debt-to-Income Ratio: {decision.dti * 100:.2f}%")
        print(f"  Reason: {decision.reason}")

    print("\n" + "=" * 80)
    print("END OF TEST CASES")
    print("=" * 80)

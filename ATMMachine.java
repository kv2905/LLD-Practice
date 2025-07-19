/*
I am solving this problem in order to learn Chain of Responsibilty Design Pattern
Problem Statement: An ATM needs to dispense cash in denominations of ₹2000, ₹500, and ₹100.
Given a withdrawal amount (e.g., ₹3700), it should use the highest possible denominations first and delegate to the next handler for the remaining amount.
*/

// ======== Imports ========== //

import java.util.*;

// ======= Main Test ======== //

public class ATMMain {
    public static void main(String[] args) {
        int amount = 50;

        Validator amountValidator = new ValidatorFactory().getValidator();
        CashDispenser cashDispenser = new CashDispenserFactory().getCashDispenser();

        ATMMachine atm = new ATMMachine(amountValidator, cashDispenser);

        Map<Integer, Integer> result = new LinkedHashMap<>();

        atm.processAmount(amount, result);

        printResult(result);
    }

    private static void printResult(Map<Integer, Integer> result) {
        for (var entry : result.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}

// =========== ATM Machine ============= //

public class ATMMachine {
    private Validator validator;
    private CashDispenser cashDispenser;

    public ATMMachine(Validator validator, CashDispenser cashDispenser) {
        this.validator = validator;
        this.cashDispenser = cashDispenser;
    }

    private boolean validateAmount(int amount) {
        return this.validator.validate(amount);
    }

    private void dispenseCash(int amount, Map<Integer, Integer> result) {
        this.cashDispenser.dispense(amount, result);
    }

    public void processAmount(int amount, Map<Integer, Integer> result) {
        System.out.println("Withdrawing amount: " + amount);

        if (!this.validateAmount(amount)) {
            System.out.println("Please enter a valid amount, minimum should be 50 and should a multiple of 50");

            return;
        }

        this.dispenseCash(amount, result);
    }
}

// ========== Validator Factory =============== //

class ValidatorFactory {
    public Validator getValidator() {
        Validator validator = new MinimumAmountValidator();
        validator.setNext(new MultipleOfFiftyValidator());

        return validator;
    }
}

// ========== Cash Dispenser Factory ========= //

class CashDispenserFactory {
    public CashDispenser getCashDispenser() {
        CashDispenser atm = new TwoThousandCashDispenser();
        atm.setNext(new FiveHundredCashDispenser())
           .setNext(new OneHundredCashDispenser())
           .setNext(new FiftyCashDispenser());

        return atm;
    }
}

// ========= Validation Chain of Responsibility ========= //

abstract class Validator {
    protected Validator nextValidator;

    public Validator setNext(Validator validator) {
        this.nextValidator = validator;
        return this.nextValidator;
    }

    public boolean validate(int amount) {
        boolean isValid = this.getValidation(amount);
        if (!isValid) return false;
        if (this.nextValidator != null) {
            return this.nextValidator.validate(amount);
        }
        return true;
    }

    public abstract boolean getValidation(int amount);
}

class MinimumAmountValidator extends Validator {
    public boolean getValidation(int amount) {
        return amount >= 50;
    }
}

class MultipleOfFiftyValidator extends Validator {
    public boolean getValidation(int amount) {
        return amount % 50 == 0;
    }
}

// ======== Dispensing Chain of Responsibility ========= //

abstract class CashDispenser {
    protected CashDispenser nextCashDispenser;

    public CashDispenser setNext(CashDispenser CashDispenser) {
        this.nextCashDispenser = CashDispenser;
        return this.nextCashDispenser;
    }

    public void dispense(int amount, Map<Integer, Integer> result) {
        int denomination = this.getDenomination();
        if (amount >= denomination) {
            int count = amount / denomination;
            int remainder = amount % denomination;
            result.put(denomination, result.getOrDefault(denomination, 0) + count);
            if (remainder > 0 && this.nextCashDispenser != null) {
                this.nextCashDispenser.dispense(remainder, result);
            } else if (remainder > 0) {
                System.out.println("Cannot dispense ₹" + remainder + ". No handler.");
            }
        } else if(this.nextCashDispenser != null) {
            this.nextCashDispenser.dispense(amount, result);
        } else {
            System.out.println("Cannot dispense ₹" + amount + ". No handler.");
        }
    }

    public abstract int getDenomination();
}

class TwoThousandCashDispenser extends CashDispenser {
    public int getDenomination() {
        return 2000;
    }
}

class FiveHundredCashDispenser extends CashDispenser {
    public int getDenomination() {
        return 500;
    }
}

class OneHundredCashDispenser extends CashDispenser {
    public int getDenomination() {
        return 100;
    }
}

class FiftyCashDispenser extends CashDispenser {
    public int getDenomination() {
        return 50;
    }
}

// ========= End of Program ========= //
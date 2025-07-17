/*
I am solving this problem in order to learn Chain of Responsibilty Design Pattern
Problem Statement: An ATM needs to dispense cash in denominations of ₹2000, ₹500, and ₹100.
Given a withdrawal amount (e.g., ₹3700), it should use the highest possible denominations first and delegate to the next handler for the remaining amount.
*/

public class ATMMain {
    public static void main(String[] args) {
        CashDispenser atm = new TwoThousandCashDispenser();
        atm.setNext(new FiveHundredCashDispenser())
           .setNext(new OneHundredCashDispenser())
           .setNext(new FiftyCashDispenser());

        System.out.println("Withdraw ₹3700:");
        atm.dispense(3700);

        System.out.println("\nWithdraw ₹250:");
        atm.dispense(250);
    }
}

abstract class CashDispenser {
    CashDispenser nextCashDispenser;

    public CashDispenser setNext(CashDispenser CashDispenser) {
        this.nextCashDispenser = CashDispenser;
        return this.nextCashDispenser;
    }

    public void dispense(int amount) {
        int denomination = this.getDenomination();
        if (amount >= denomination) {
            int count = amount / denomination;
            int remainder = amount % denomination;
            System.out.println("Dispensing " + count + " x ₹" + denomination);
            if (remainder > 0 && this.nextCashDispenser != null) {
                this.nextCashDispenser.dispense(remainder);
            } else if (remainder > 0) {
                System.out.println("Cannot dispense ₹" + remainder + ". No handler.");
            }
        } else if(this.nextCashDispenser != null) {
            this.nextCashDispenser.dispense(amount);
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
/*
I am solving this problem in order to learn Chain of Responsibilty Design Pattern
Problem Statement: An ATM needs to dispense cash in denominations of ₹2000, ₹500, and ₹100.
Given a withdrawal amount (e.g., ₹3700), it should use the highest possible denominations first and delegate to the next handler for the remaining amount.
*/

public class ATMMain {
    public static void main(String[] args) {
        CashDispencer atm = new TwoThousandCashDispencer();
        atm.setNext(new FiveHunderedCashDispencer())
           .setNext(new OneHunderedCashDispencer());

        System.out.println("Withdraw ₹3700:");
        atm.dispense(3700);

        System.out.println("\nWithdraw ₹250:");
        atm.dispense(250);
    }
}

abstract class CashDispencer {
    CashDispencer nextCashDispencer;

    public CashDispencer setNext(CashDispencer cashDispencer) {
        this.nextCashDispencer = cashDispencer;
        return this.nextCashDispencer;
    }

    public void dispense(int amount) {
        int denomination = this.getDenomination();
        if (amount >= denomination) {
            int count = amount / denomination;
            int reminder = amount % denomination;
            System.out.println("Dispensing " + count + " x ₹" + denomination);
            if (reminder > 0 && this.nextCashDispencer != null) {
                this.nextCashDispencer.dispense(reminder);
            } else if (reminder > 0) {
                System.out.println("Cannot dispense ₹" + reminder + ". No handler.");
            }
        } else if(this.nextCashDispencer != null) {
            this.nextCashDispencer.dispense(amount);
        } else {
            System.out.println("Cannot dispense ₹" + amount + ". No handler.");
        }
    }

    public abstract int getDenomination();
}

class TwoThousandCashDispencer extends CashDispencer {
    public int getDenomination() {
        return 2000;
    }
}

class FiveHunderedCashDispencer extends CashDispencer {
    public int getDenomination() {
        return 500;
    }
}

class OneHunderedCashDispencer extends CashDispencer {
    public int getDenomination() {
        return 100;
    }
}
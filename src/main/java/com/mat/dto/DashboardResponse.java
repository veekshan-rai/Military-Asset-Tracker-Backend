package com.mat.dto;

/**
 * DashboardResponse DTO
 *
 * Contains all the dashboard metrics for the frontend.
 *
 * Business formula:
 *   Net Movement   = Purchases + Transfer In - Transfer Out
 *   Closing Balance = Opening Balance + Net Movement
 */
public class DashboardResponse {

    private int openingBalance;
    private int closingBalance;
    private int netMovement;
    private int purchases;
    private int transferIn;
    private int transferOut;
    private int assigned;
    private int expended;

    // ── Constructors ──────────────────────────────────────

    public DashboardResponse() {
    }

    // ── Getters and Setters ───────────────────────────────

    public int getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(int openingBalance) {
        this.openingBalance = openingBalance;
    }

    public int getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(int closingBalance) {
        this.closingBalance = closingBalance;
    }

    public int getNetMovement() {
        return netMovement;
    }

    public void setNetMovement(int netMovement) {
        this.netMovement = netMovement;
    }

    public int getPurchases() {
        return purchases;
    }

    public void setPurchases(int purchases) {
        this.purchases = purchases;
    }

    public int getTransferIn() {
        return transferIn;
    }

    public void setTransferIn(int transferIn) {
        this.transferIn = transferIn;
    }

    public int getTransferOut() {
        return transferOut;
    }

    public void setTransferOut(int transferOut) {
        this.transferOut = transferOut;
    }

    public int getAssigned() {
        return assigned;
    }

    public void setAssigned(int assigned) {
        this.assigned = assigned;
    }

    public int getExpended() {
        return expended;
    }

    public void setExpended(int expended) {
        this.expended = expended;
    }
}

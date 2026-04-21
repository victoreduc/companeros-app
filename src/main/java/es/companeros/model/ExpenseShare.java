package es.companeros.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "expense_shares")
public class ExpenseShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    private Long userId;

    @Column(precision = 10, scale = 2)
    private BigDecimal amountOwed;

    private boolean paid = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Expense getExpense() { return expense; }
    public void setExpense(Expense expense) { this.expense = expense; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmountOwed() { return amountOwed; }
    public void setAmountOwed(BigDecimal amountOwed) { this.amountOwed = amountOwed; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
}

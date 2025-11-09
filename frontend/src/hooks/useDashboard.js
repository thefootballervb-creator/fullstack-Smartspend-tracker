import { useEffect, useState, useCallback } from "react";
import useCategories from "./useCategories";
import UserService from "../services/userService";
import AuthService from "../services/auth.service";

function useDashboard(currentMonth) {
    const [total_income, setIncome] = useState(0)
    const [total_expense, setExpense] = useState(0)
    const [no_of_transactions, setTransactions] = useState(0)
    const cash_in_hand = Number((total_income - total_expense)?.toFixed(2)) || 0;
    const [categories] = useCategories()
    const [categorySummary, setCategorySummary] = useState([])
    const [budgetAmount, setBudgetAmount] = useState(0)
    const [isLoading, setIsLoading] = useState(true);
    const [isError, setIsError] = useState(false);


    const generateTransactionSummary = useCallback(async () => {
        setIsLoading(true)
        await UserService.getTotalIncomeOrExpense(AuthService.getCurrentUser().id, 2, currentMonth.id, currentMonth.year).then(
            (response) => {
                if (response && response.data && response.data.status === "SUCCESS") {
                    setIncome(Number((response.data.response) ? response.data.response.toFixed(2) : 0))
                }
            },
            (error) => {
                console.error("Error fetching income:", error)
                setIsError(true)
            }
        )

        await UserService.getTotalIncomeOrExpense(AuthService.getCurrentUser().id, 1, currentMonth.id, currentMonth.year).then(
            (response) => {
                if (response && response.data && response.data.status === "SUCCESS") {
                    setExpense(Number((response.data.response) ? response.data.response.toFixed(2) : 0))
                }
            },
            (error) => {
                console.error("Error fetching expense:", error)
                setIsError(true)
            }
        )

        await UserService.getTotalNoOfTransactions(AuthService.getCurrentUser().id, currentMonth.id, currentMonth.year).then(
            (response) => {
                if (response && response.data && response.data.status === "SUCCESS") {
                    setTransactions(response.data.response || 0)
                }
            },
            (error) => {
                console.error("Error fetching transaction count:", error)
                setIsError(true)
            }
        )
        setIsLoading(false)

    }, [currentMonth])

    const generateCategorySummary = useCallback(async () => {
        setIsLoading(true)
        const filtered = [];
        await Promise.all(categories.filter(cat => cat.transactionType.transactionTypeId === 1).map(async (cat) => {
            try {
                const response = await UserService.getTotalByCategory(AuthService.getCurrentUser().email, cat.categoryId, currentMonth.id, currentMonth.year);
                if (response && response.data && response.data.status === "SUCCESS" && response.data.response) {
                    filtered.push({ name: cat.categoryName, amount: Number(response.data.response ? response.data.response.toFixed(2) : 0) });
                }
            } catch (error) {
                console.error("Error fetching category summary:", error)
                setIsError(true)
            }
        }));
        setCategorySummary(filtered)
        setIsLoading(false)
    }, [categories, currentMonth])

    const fetchBudget = useCallback(async () => {
        setIsLoading(true)
        await UserService.getBudget(currentMonth.id, currentMonth.year)
            .then((response) => {
                if (response && response.data && response.data.status === "SUCCESS") {
                    setBudgetAmount(response.data.response || 0)
                }
            })
            .catch((error) => {
                console.error("Error fetching budget:", error)
                setIsError(true)
            })
        setIsLoading(false)
    }, [currentMonth])

    const saveBudget = async (d) => {
        await UserService.createBudget(d.amount)
            .catch((error) => {
                setIsError(true)
            })
        fetchBudget()
    }

    useEffect(() => {
        generateTransactionSummary()
        if (categories) {
            generateCategorySummary()
        }
        fetchBudget()
    }, [currentMonth, categories, generateTransactionSummary, generateCategorySummary, fetchBudget])

    return [
        total_expense,
        total_income,
        cash_in_hand,
        no_of_transactions,
        categorySummary,
        budgetAmount,
        saveBudget,
        isLoading,
        isError
    ]


}

export default useDashboard;
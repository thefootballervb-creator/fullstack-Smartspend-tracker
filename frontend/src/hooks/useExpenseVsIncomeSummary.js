import { useCallback, useEffect, useState } from "react";
import UserService from "../services/userService";
import AuthService from "../services/auth.service";

function useExpenseVsIncomeSummary(months) {
    const [data, setData] = useState([]);
    const [isError, setIsError] = useState(false);
    const [isLoading, setIsLoading] = useState(true)

    const generateData = useCallback((fetchedData) => {
        const finalData = months.map(({ id, monthName }) => {
            const monthData = fetchedData.find((t) => t.month === id)
            return {
                id, monthName,
                totalIncome: monthData ? monthData.total_income : 0,
                totalExpense: monthData ? monthData.total_expense : 0
            }
        })
        setData(finalData)
    }, [months])

    useEffect(() => {
        const getData = async () => {
            await UserService.getMonthlySummary(AuthService.getCurrentUser().email).then(
                (response) => {
                    if (response.data.status === "SUCCESS") {
                        generateData(response.data.response)
                    }
                },
                () => {
                    setIsError(true)
                }
            )
            setIsLoading(false)
        }

        getData()
    }, [generateData])

    return [data, isLoading, isError]
}

export default useExpenseVsIncomeSummary;
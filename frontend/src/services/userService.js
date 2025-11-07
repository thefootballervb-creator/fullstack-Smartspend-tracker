import axios from "axios";
import AuthService from "./auth.service";
import API_BASE_URL from "./auth.config";


const get_categories = () => {
    return axios.get(
        API_BASE_URL + '/category/getAll',
        {
            headers: AuthService.authHeader()
        }
    )
}

const add_transaction = (email, categoryId, descripiton, amount, date) => {
    return axios.post(
        API_BASE_URL + '/transaction/new', 
        {
            userEmail: email,
            categoryId: categoryId,
            description:descripiton,
            amount: amount,
            date: date 
        },
        {
            headers: AuthService.authHeader()
        }
    )
}

const get_transactions = (email, pageNumber, pageSize, searchKey, sortField, sortDirec, transactionType) => {
    return axios.get(
        API_BASE_URL + '/transaction/getByUser', 
        {
            headers: AuthService.authHeader(), 
            params: {
                email: email, pageNumber: pageNumber, pageSize: pageSize, searchKey: searchKey, sortField: sortField, sortDirec: sortDirec, transactionType: transactionType
            }
        }
    )
}

const get_single_transaction = (id) => {
    return axios.get(
        API_BASE_URL + '/transaction/getById', 
        {
            headers: AuthService.authHeader(), 
            params: {
                id: id
            }
        }
    )
}
const update_transaction = (transactionId, email, categoryId, descripiton, amount, date) => {
    return axios.put(API_BASE_URL + '/transaction/update', 
        {
            userEmail: email,
            categoryId: categoryId,
            description:descripiton,
            amount: amount,
            date: date 
        },
        {
            headers: AuthService.authHeader(),
            params: {
                transactionId: transactionId
            }
        }
    )
}

const delete_transaction = (id) => {
    return axios.delete(
        API_BASE_URL + '/transaction/delete', 
        {
            headers: AuthService.authHeader(), 
            params: {
                transactionId: id
            }
        }
    )
}

const settingsResetPassword = async (email, oldPassword, newpassword) => {
    return await axios.post(API_BASE_URL + '/user/settings/changePassword', {
        email: email, 
        currentPassword: oldPassword,
        newPassword: newpassword
    }, {
      headers: AuthService.authHeader(),
    })
}

const getTotalIncomeOrExpense = async (userId, transactionTypeId, month, year) => {
    return axios.get(
        API_BASE_URL + '/report/getTotalIncomeOrExpense', 
        {
            headers: AuthService.authHeader(), 
            params: {
                userId: userId,
                transactionTypeId: transactionTypeId,
                month: month,
                year: year
            }
        }
    )
}

const getTotalNoOfTransactions = async (userId, month, year) => {
    return axios.get(
        API_BASE_URL + '/report/getTotalNoOfTransactions', 
        {
            headers: AuthService.authHeader(), 
            params: {
                userId: userId,
                month: month,
                year: year
            }
        }
    )
}

const getTotalByCategory = async (email, categoryId, month, year) => {
    return await axios.get(
        API_BASE_URL + '/report/getTotalByCategory', 
        {
            headers: AuthService.authHeader(), 
            params: {
                email: email,
                categoryId: categoryId,
                month: month,
                year: year
            }
        }
    )
}

const getMonthlySummary = async (email) => {
    return await axios.get(
        API_BASE_URL + '/report/getMonthlySummaryByUser', 
        {
            headers: AuthService.authHeader(), 
            params: {
                email: email,
            }
        }
    )
}

const getBudget = async (month, year) => {
    return await axios.get(
        API_BASE_URL + '/budget/get', 
        {
            headers: AuthService.authHeader(), 
            params: {
                userId: AuthService.getCurrentUser().id,
                month: month,
                year: year
            }
        }
    )
}

const createBudget = (amount) => {
    return axios.post(
        API_BASE_URL + '/budget/create', 
        {
            userId: AuthService.getCurrentUser().id,
            amount: amount
        },
        {
            headers: AuthService.authHeader()
        }
    )
}

const uploadProfileImg = async (formData) => {
    return await axios.post(
        API_BASE_URL + '/user/settings/profileImg', 
        formData,
        {
            headers: AuthService.authHeader(),
            
        }
    )
}
const getProfileImg = async () => {
    return await axios.get(
        API_BASE_URL + '/user/settings/profileImg', 
        {
            headers: AuthService.authHeader(),
            params: {
                email: AuthService.getCurrentUser().email
            }
        }
    )
}

const removeProfileImg = async () => {
    return await axios.delete(
        API_BASE_URL + '/user/settings/profileImg', 
        {
            headers: AuthService.authHeader(),
            params: {
                email: AuthService.getCurrentUser().email
            }
        }
    )
}

const createSavedTransaction = (categoryId, amount, description, frequency, upcomingDate) => {
    return axios.post(
        API_BASE_URL + '/saved/create', 
        {
            userId: AuthService.getCurrentUser().id,
            categoryId: categoryId,
            amount:amount,
            description:description,
            frequency: frequency,
            upcomingDate: upcomingDate
        },
        {
            headers: AuthService.authHeader()
        }
    )
}

const getSavedTransactions = () => {
    return axios.get(
        API_BASE_URL + '/saved/user', 
        {
            headers: AuthService.authHeader(), 
            params: {
                id: AuthService.getCurrentUser().id
            }
        }
    )
}

const getSavedTransactionById = (id) => {
    return axios.get(
        API_BASE_URL + '/saved/', 
        {
            headers: AuthService.authHeader(), 
            params: {
                id: id
            }
        }
    )
}
const updateSavedTransaction = (id, categoryId, amount, description, frequency, upcomingDate) => {
    return axios.put(API_BASE_URL + '/saved/', 
        {
            userId: AuthService.getCurrentUser().id,
            categoryId: categoryId,
            amount:amount,
            description:description,
            frequency: frequency,
            upcomingDate: upcomingDate
        },
        {
            headers: AuthService.authHeader(),
            params: {
                id: id
            }
        }
    )
}

const deleteSavedTransaction = (id) => {
    return axios.delete(
        API_BASE_URL + '/saved/', 
        {
            headers: AuthService.authHeader(), 
            params: {
                id: id
            }
        }
    )
}

const addSavedTransaction = (id) => {
    return axios.get(
        API_BASE_URL + '/saved/add', 
        {
            headers: AuthService.authHeader(),
            params: {
                id: id
            }
        }
    )
}

const skipSavedTransaction = (id) => {
    return axios.get(
        API_BASE_URL + '/saved/skip', 
        {
            headers: AuthService.authHeader(),
            params: {
                id: id
            }
        }
    )
}

// Exports
const exportPdf = async (params) => {
    try {
        const response = await axios.get(API_BASE_URL + '/report/export/pdf', {
            headers: AuthService.authHeader(),
            params,
            responseType: 'blob'
        })
        // Check if response is actually a blob (not an error)
        if (response.data instanceof Blob && response.data.size > 0) {
            return response.data;
        }
        throw new Error('Invalid PDF response');
    } catch (error) {
        // If error response is a blob, try to parse it as text
        if (error.response && error.response.data instanceof Blob) {
            const text = await error.response.data.text();
            try {
                const json = JSON.parse(text);
                throw new Error(json.message || 'Export PDF failed');
            } catch (e) {
                throw new Error('Export PDF failed: ' + text);
            }
        }
        throw error;
    }
}

const exportExcel = async (params) => {
    try {
        const response = await axios.get(API_BASE_URL + '/report/export/excel', {
            headers: AuthService.authHeader(),
            params,
            responseType: 'blob'
        })
        // Check if response is actually a blob (not an error)
        if (response.data instanceof Blob && response.data.size > 0) {
            return response.data;
        }
        throw new Error('Invalid Excel response');
    } catch (error) {
        // If error response is a blob, try to parse it as text
        if (error.response && error.response.data instanceof Blob) {
            const text = await error.response.data.text();
            try {
                const json = JSON.parse(text);
                throw new Error(json.message || 'Export Excel failed');
            } catch (e) {
                throw new Error('Export Excel failed: ' + text);
            }
        }
        throw error;
    }
}

const UserService = {
    get_categories,
    add_transaction ,
    get_transactions,
    get_single_transaction,
    update_transaction,
    delete_transaction,
    settingsResetPassword,
    getTotalIncomeOrExpense,
    getTotalNoOfTransactions,
    getTotalByCategory,
    getMonthlySummary,
    getBudget,
    createBudget,
    uploadProfileImg,
    getProfileImg,
    removeProfileImg,
    createSavedTransaction,
    getSavedTransactions,
    getSavedTransactionById,
    updateSavedTransaction,
    deleteSavedTransaction,
    addSavedTransaction,
    skipSavedTransaction,
    exportPdf,
    exportExcel
}
export default UserService;
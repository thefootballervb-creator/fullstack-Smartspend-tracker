import { useEffect, useState, useCallback } from "react";
import AdminService from "../../services/adminService"
import Header from "../../components/utils/header";
import Loading from "../../components/utils/loading";
import usePagination from "../../hooks/usePagination";
import Search from "../../components/utils/search";
import PageInfo from "../../components/utils/pageInfo";
import Info from "../../components/utils/Info";
import Container from "../../components/utils/Container";
import toast, { Toaster } from "react-hot-toast";

function AdminUsersManagement() {

    const [data, setData] = useState([]);
    const [isFetching, setIsFetching] = useState(true);

    const {
        pageSize, pageNumber, noOfPages, searchKey,
        onNextClick, onPrevClick, setNoOfPages, setNoOfRecords, setSearchKey, getPageInfo
    } = usePagination()

    const getUsers = useCallback(async () => {
        await AdminService.getAllUsers(pageNumber, pageSize, searchKey).then(
            (response) => {
                if (response.data.status === 'SUCCESS') {
                    setData(response.data.response.data)
                    setNoOfPages(response.data.response.totalNoOfPages)
                    setNoOfRecords(response.data.response.totalNoOfRecords)
                    return
                }
                toast.error("Failed to fetch all users: Try again later!")
            },
            (error) => {
                toast.error("Failed to fetch all users: Try again later!")
            }
        )
        setIsFetching(false)
    }, [pageNumber, pageSize, searchKey, setNoOfPages, setNoOfRecords])

    const disableOrEnable = async (userId) => {
        await AdminService.disableOrEnableUser(userId).then(
            (response) => {
                if (response.data.status === 'SUCCESS') {
                    window.location.reload()
                    return
                }
                toast.error("Failed to update user: Try again later!")
            },
            (error) => {
                toast.error("Failed to update user: Try again later!")
            }
        )
    }

    const deleteUser = async (userId, username) => {
        if (!window.confirm(`Are you sure you want to delete user "${username}"? This action cannot be undone and will delete all their transactions and data.`)) {
            return
        }

        await AdminService.deleteUser(userId).then(
            (response) => {
                if (response.data.status === 'SUCCESS') {
                    toast.success("User deleted successfully!")
                    window.location.reload()
                    return
                }
                toast.error("Failed to delete user: Try again later!")
            },
            (error) => {
                toast.error("Failed to delete user: Try again later!")
            }
        )
    }

    useEffect(() => {
        getUsers();
    }, [getUsers])

    return (
        <Container activeNavId={5}>
            <Header title="Users" />
            <Toaster/>

            {(isFetching) && <Loading />}
            {(!isFetching) &&
                <>
                    <div className="utils page">
                        <Search onChange={(val) => setSearchKey(val)} placeholder="Search users" />
                        <PageInfo info={getPageInfo()} onPrevClick={onPrevClick} onNextClick={onNextClick}
                            pageNumber={pageNumber} noOfPages={noOfPages}
                        />
                    </div>
                    {(data.length === 0) && <Info text={"No transactions found!"} />}
                    {(data.length !== 0) && (
                        <table>
                            <UsersTableHeader />
                            <UsersTableBody data={data} disableOrEnable={disableOrEnable} deleteUser={deleteUser} />
                        </table>
                    )}
                </>
            }
        </Container>
    )
}

export default AdminUsersManagement;


function UsersTableHeader() {
    return (
        <tr>
            <th>User Id</th> <th>Username</th> <th>Email</th>
            <th>Tot. Expense(Rs.)</th> <th>Tot. Income(Rs.)</th>
            <th>Tot. No. Transactions</th> <th>Status</th> <th>Action</th>
        </tr>
    )
}

function UsersTableBody({ data, disableOrEnable, deleteUser }) {
    return (
        data.map((item) => {
            return (
                <tr key={item.id}>
                    <td>{"U" + String(item.id).padStart(5, '0')}</td>
                    <td>{item.username}</td>
                    <td>{item.email}</td>
                    <td>Rs. {item.expense || 0.0}</td>
                    <td>Rs. {item.income || 0.0}</td>
                    <td>{item.noOfTransactions || 0}</td>
                    {
                        item.enabled ? <td style={{ color: '#6aa412' }}>Enabled</td> : <td style={{ color: '#ff0000' }}>Disabled</td>
                    }

                    <td>
                        <div style={{ display: 'flex', gap: '8px' }}>
                            {
                                (item.enabled) ?
                                    <button
                                        onClick={() => disableOrEnable(item.id)}
                                        style={{ backgroundColor: '#ff0000', color: 'white', padding: '5px 10px', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                                    >Disable
                                    </button> :
                                    <button
                                        onClick={() => disableOrEnable(item.id)}
                                        style={{ backgroundColor: '#6aa412', color: 'white', padding: '5px 10px', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                                    >Enable
                                    </button>
                            }
                            <button
                                onClick={() => deleteUser(item.id, item.username)}
                                style={{ backgroundColor: '#dc3545', color: 'white', padding: '5px 10px', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                            >Delete
                            </button>
                        </div>
                    </td>
                </tr>
            )
        })
    )
}
import { useCallback, useEffect, useState } from 'react';
import UserService from '../../services/userService';
import AuthService from '../../services/auth.service';
import Header from '../../components/utils/header';
import Loading from '../../components/utils/loading';
import Search from '../../components/utils/search';
import usePagination from '../../hooks/usePagination';
import PageInfo from '../../components/utils/pageInfo';
import TransactionList from '../../components/userTransactions/transactionList.js';
import { useLocation, useNavigate } from 'react-router-dom';
import Info from '../../components/utils/Info.js';
import Container from '../../components/utils/Container.js';
import toast, { Toaster } from 'react-hot-toast';


function Transactions() {

    const [userTransactions, setUserTransactions] = useState([]);
    const [isFetching, setIsFetching] = useState(true);
    const [transactionType, setTransactionType] = useState('')
    const [from, setFrom] = useState("")
    const [to, setTo] = useState("")
    const [min, setMin] = useState("")
    const [max, setMax] = useState("")
    const [categories, setCategories] = useState([])
    const [selectedCategoryId, setSelectedCategoryId] = useState("")
    const location = useLocation();
    const navigate = useNavigate();

    const {
        pageSize, pageNumber, noOfPages, sortField, sortDirec, searchKey,
        onNextClick, onPrevClick, setNoOfPages, setNoOfRecords, setSearchKey, getPageInfo
    } = usePagination('date')

    const getTransactions = useCallback(async () => {
        await UserService.get_transactions(AuthService.getCurrentUser().email, pageNumber,
            pageSize, searchKey, sortField, sortDirec, transactionType).then(
                (response) => {
                    if (response.data.status === "SUCCESS") {
                        setUserTransactions(response.data.response.data)
                        setNoOfPages(response.data.response.totalNoOfPages)
                        setNoOfRecords(response.data.response.totalNoOfRecords)
                        return
                    }
                },
                (error) => {
                    toast.error("Failed to fetch all transactions: Try again later!")
                }
            )
        setIsFetching(false)
    }, [pageNumber, pageSize, searchKey, sortField, sortDirec, transactionType, setNoOfPages, setNoOfRecords])

    useEffect(() => {
        getTransactions()
    }, [getTransactions])

    useEffect(() => {
        if (location.state?.text) {
            toast.success(location.state.text)
            navigate(location.pathname, { replace: true, state: null })
        }
    }, [location, navigate])

    useEffect(() => {
        (async ()=>{
            try{ const res = await UserService.get_categories(); setCategories(res.data.response || []);}catch(e){}
        })();
    }, [])

    return (
        <Container activeNavId={1}>
            <Header title="Transactions History" />
            <Toaster/>

            {(userTransactions.length === 0 && isFetching) && <Loading />}
            {(!isFetching) &&
                <>
                    <div className='utils'>
                        <Filter
                            setTransactionType={(val) => setTransactionType(val)}
                        />
                        <div style={{display:'inline-flex', gap:'8px', alignItems:'center'}}>
                            <input type='date' value={from} onChange={e=>setFrom(e.target.value)} />
                            <input type='date' value={to} onChange={e=>setTo(e.target.value)} />
                            <select value={selectedCategoryId} onChange={e=>setSelectedCategoryId(e.target.value)}>
                                <option value=''>All categories</option>
                                {categories && categories.map(c=> <option key={c.categoryId} value={c.categoryId}>{c.categoryName}</option>)}
                            </select>
                            <input type='number' placeholder='Min' value={min} onChange={e=>setMin(e.target.value)} style={{width:90}}/>
                            <input type='number' placeholder='Max' value={max} onChange={e=>setMax(e.target.value)} style={{width:90}}/>
                        </div>
                        <div className='page'>
                            <Search
                                onChange={(val) => setSearchKey(val)}
                                placeholder="Search transactions"
                            />
                            <PageInfo
                                info={getPageInfo()}
                                onPrevClick={onPrevClick}
                                onNextClick={onNextClick}
                                pageNumber={pageNumber}
                                noOfPages={noOfPages}
                            />
                        </div>
                    </div>
                    {(userTransactions.length === 0) && <Info text={"No transactions found!"} />}
                    {(userTransactions.length !== 0) && <TransactionList list={userTransactions} />}
                    <div style={{display:'flex', justifyContent:'center', gap: '8px', marginTop: '20px', padding: '20px'}}>
                        <button onClick={async ()=>{
                            try{
                                const params = {
                                    email: AuthService.getCurrentUser().email,
                                    ...(from ? { from } : {}),
                                    ...(to ? { to } : {}),
                                    ...(selectedCategoryId ? { categoryId: parseInt(selectedCategoryId) } : {}),
                                    ...(min ? { min: parseFloat(min) } : {}),
                                    ...(max ? { max: parseFloat(max) } : {})
                                };
                                const blob = await UserService.exportPdf(params);
                                if (!blob || (blob.size !== undefined && blob.size === 0)) {
                                    toast.error('Export PDF failed: Empty response');
                                    return;
                                }
                                const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }));
                                const link = document.createElement('a');
                                link.href = url; link.download = 'transactions.pdf'; link.click();
                                window.URL.revokeObjectURL(url);
                                toast.success('PDF exported successfully!');
                            }catch(e){
                                console.error('PDF export error:', e);
                                toast.error('Export PDF failed: ' + (e.response?.data?.message || e.message || 'Unknown error'));
                            }
                        }}>Export PDF</button>
                        <button onClick={async ()=>{
                            try{
                                const params = {
                                    email: AuthService.getCurrentUser().email,
                                    ...(from ? { from } : {}),
                                    ...(to ? { to } : {}),
                                    ...(selectedCategoryId ? { categoryId: parseInt(selectedCategoryId) } : {}),
                                    ...(min ? { min: parseFloat(min) } : {}),
                                    ...(max ? { max: parseFloat(max) } : {})
                                };
                                const blob = await UserService.exportExcel(params);
                                if (!blob || (blob.size !== undefined && blob.size === 0)) {
                                    toast.error('Export Excel failed: Empty response');
                                    return;
                                }
                                const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }));
                                const link = document.createElement('a');
                                link.href = url; link.download = 'transactions.xlsx'; link.click();
                                window.URL.revokeObjectURL(url);
                                toast.success('Excel exported successfully!');
                            }catch(e){
                                console.error('Excel export error:', e);
                                toast.error('Export Excel failed: ' + (e.response?.data?.message || e.message || 'Unknown error'));
                            }
                        }}>Export Excel</button>
                    </div>
                </>
            }
        </Container>
    )
}

export default Transactions;


function Filter({ setTransactionType }) {
    return (
        <select onChange={(e) => setTransactionType(e.target.value)} style={{ margin: '0 15px 0 0' }}>
            <option value="">All</option>
            <option value="expense">Expense</option>
            <option value="income">Income</option>
        </select>
    )
}



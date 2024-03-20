import React, { useState, useEffect } from 'react';
import './App.css';
import TransactionList from './TransactionList';
import SendMoneyForm from './SendMoneyForm';

function App() {
  const [transactions, setTransactions] = useState([]);
  const [pageCount, setPageCount] = useState(0);
  const [connections, setConnections] = useState([]);

  useEffect(() => {

  const fetchConnections = async () => {
    try {
      const response = await fetch('/api/v1/users/my-connections');
      if (!response.ok) throw new Error('Network response was not ok');
      const data = await response.json();
      setConnections(data);
    } catch (error) {
      console.error('Fetch error:', error);
    }
  };

    fetchTransactions();
    fetchConnections();
  }, []);

    const fetchTransactions = async () => {
      try {
        const response = await fetch('/api/v1/transactions/my-transactions-paginated');
        if (!response.ok) throw new Error('Network response was not ok');
        const data = await response.json();
        setTransactions(data);
        const { totalPages } = data;
        setPageCount(data.totalPages);

      } catch (error) {
        console.error('Fetch error:', error);
      }
    };

const onPageChange = async ({ selected }) => {
  const pageNumber = selected;

  try {
    const response = await fetch(`/api/v1/transactions/my-transactions-paginated?page=${pageNumber}`);
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    const data = await response.json();

    setTransactions(data);
  } catch (error) {
    console.error('Error fetching transactions:', error);
    // Handle errors, such as displaying a message to the user
  }
};

  return (
    <div className="App">
      <SendMoneyForm connections={connections} onSuccessfulPayment={fetchTransactions} />
      <TransactionList transactions={transactions} pageCount={pageCount} onPageChange={onPageChange} />
    </div>
  );
}

export default App;

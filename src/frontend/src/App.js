import React, { useState, useEffect } from 'react';
import './App.css';
import TransactionList from './TransactionList';

function App() {
  const [transactions, setTransactions] = useState([]);

  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        const response = await fetch('/api/v1/transactions/my-transactions');
        if (!response.ok) throw new Error('Network response was not ok');
        const data = await response.json();
        setTransactions(data);
      } catch (error) {
        console.error('Fetch error:', error);
      }
    };

    fetchTransactions();
  }, []);


  return (
    <div className="App">
      <TransactionList transactions={transactions} />
    </div>
  );
}

export default App;

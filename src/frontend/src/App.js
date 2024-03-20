import React, { useState, useEffect } from 'react';
import './App.css';
import TransactionList from './TransactionList';
import SendMoneyForm from './SendMoneyForm';

function App() {
  const [transactions, setTransactions] = useState([]);
  const [connections, setConnections] = useState([]);

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


  return (
    <div className="App">
      <SendMoneyForm connections={connections} />
      <TransactionList transactions={transactions} />
    </div>
  );
}

export default App;

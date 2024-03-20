import React, { useState } from 'react';
import './SendMoneyForm.css';


function SendMoneyForm({ connections }) {
  const [selectedConnection, setSelectedConnection] = useState('');
  const [amount, setAmount] = useState('');
  const [selectedCurrency, setSelectedCurrency] = useState('USD');

  const handleConnectionChange = (event) => {
    setSelectedConnection(event.target.value);
  };

  const handleAmountChange = (event) => {
    const value = parseFloat(event.target.value);
    if (value >= 0) {
      setAmount(value);
    } else {
      setAmount('0');
    }
  };

  const handleCurrencyChange = (event) => {
    setSelectedCurrency(event.target.value);
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    console.log(`Sending ${amount}${selectedCurrency} to ${selectedConnection}`);
    // TODO
  };

  const onAddConnection = () => {
    console.log("adding connection");
    // TODO
  }

  return (
    <>
            <div className="send-money-header">
              <h2>Send Money</h2>
              <button className="add-connection-button" onClick={onAddConnection}>Add Connection</button>
            </div>
      <form onSubmit={handleSubmit} className="send-money-form">
        <label htmlFor="connection-select">Select A Connection:</label>
        <select id="connection-select" value={selectedConnection} onChange={handleConnectionChange}>
          <option value="">Select a connection</option>
          {connections.map(connection => (
            <option key={connection.userId} value={connection.userId}>{connection.userId}</option> // Assuming connection has 'name'
          ))}
        </select>

        <label htmlFor="amount-input">Amount:</label>
        <input type="number" id="amount-input" value={amount} onChange={handleAmountChange} min="0" />
        <label htmlFor="currency-select">Currency:</label>
        <select id="currency-select" value={selectedCurrency} onChange={handleCurrencyChange}>
          <option value="USD">USD</option>
          <option value="EUR">EUR</option>
        </select>

        <button type="submit">Send Money</button>
      </form>
    </>
  );
}

export default SendMoneyForm;
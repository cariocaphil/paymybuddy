function TransactionList({ transactions }) {
  return (
    <div className="transaction-list">
      <h2>My Transactions</h2>
      <table>
        <thead>
          <tr>
          <th>Receiver</th>
            <th>Description</th>
            <th>Amount</th>
            <th>Currency</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((transaction) => (
            <tr key={transaction.id}>
              <td>{transaction.receiverId}</td>
              <td>{transaction.description}</td>
              <td>{transaction.amount}</td>
              <td>{transaction.currency}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default TransactionList;
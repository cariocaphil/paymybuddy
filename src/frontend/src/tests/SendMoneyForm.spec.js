const { test, expect } = require('@playwright/test');

test.describe('Send Money Form', () => {
  test('should display the correct headings and button', async ({ page }) => {
    await page.goto('http://localhost:3000/');

    const sendMoneyHeading = page.locator('h2:has-text("Send Money")');
    const sendMoneyButton = page.locator('button', { hasText: 'Send Money' });

    await expect(sendMoneyHeading).toBeVisible();
    await expect(sendMoneyButton).toBeVisible();
  });
});

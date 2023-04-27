package com.atm.controller;

import com.atm.model.Account;
import com.atm.model.Transaction;
import com.atm.service.AccountService;
import com.atm.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletResponse;
// import com.atm.service.*;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

/**
 * The ATMController class which acts as a web request handler
 * Acts as a stereotype for the annotated class.
 * The dispatcher scans such annotated classes for mapped methods
 * and detects the @GettMapping and @PostMapping annotations.
 *
 * Design Pattern: This is the Controller part of the MVC pattern
 * 
 * 
 * 
 */
@Controller
public class ATMController {

    // Tells the application context to inject an instance of AccountService here
    @Autowired
    AccountService accountService;
    @Autowired
    TransactionService transactionService;

    private String message;

    private List<String> tasks = Arrays.asList("View Balance", "Withdraw", "Deposit", "Transfer","Change Pin", "Cancel Transaction");

    private String custName; 
    // private Account theAccount;

    /**
     * @GetMapping is a composed annotation that acts as a shortcut for @RequestMapping(method = RequestMethod.GET)
     * This function will handle all requests with url "/"
     * @param model Model object which can supply attributes for rendering views
     * an instance of account is created and passed into model, which will be used as a thymeleaf object in our view
     * @return String which is the name of the HTML file to be viewed
     * 
     */

    @GetMapping("/")
    public String home(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Refresh", "5;url=ATM");

        return "startup";
    }
    

    @GetMapping("/shutdown")
    public String shutdown(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Refresh", "600;url=blank");

        return "shutdown";
    }

    @GetMapping("/blank")
    public String blank() {

        return "blank";
    }

    @GetMapping("/ATM")
    public String main(Model model,HttpServletResponse httpServletResponse) {
        model.addAttribute("message", message);
        model.addAttribute("tasks", tasks);
        model.addAttribute("account", new Account());


        httpServletResponse.setHeader("Refresh", "600;url=shutdown");
        return "welcome"; //view
    }

    // @GetMapping("/shutdown")
    // public String shutdown(HttpServletResponse httpServletResponse) {
    //     httpServletResponse.setHeader("Refresh", "1800;url=about:blank");
    //     return "shutdown";
    // }
    /**
     * test function
     * @param name
     * @param model
     * @return
     */
    @GetMapping("/hello")
    public String mainWithParam(
            @RequestParam(name = "name", required = false, defaultValue = "") String name, Model model) {

        custName = name;

        model.addAttribute("message", custName);
        return "welcome"; //view
    }

    /**
     * Handles POST request for /login
     * @param formAccount contains account number and PIN
     * @param model Model object which can store login error messages while rendering the view
     * @param session HTTPSession object to store account number in the session
     * @return String which is the name of the HTML file to be viewed
     * 
     */

    @PostMapping("/login")
    public String login(@ModelAttribute Account formAccount, Model model, HttpSession session) {
        // Call the appropriate service function which performs login
        int result = accountService.checkLogin(formAccount);
    
        // Depending on the return value, add messages to the model which will be viewed
        if (result == 1) {
            model.addAttribute("error", "Account number does not exist");
            System.out.println("Login error");
            return "welcome";
        } else if (result == 2) {
            model.addAttribute("error", "Incorrect pin");
            System.out.println("Login error");
            return "welcome";
        } else if (result == 3) {
            model.addAttribute("error", "Account locked go to the bank and get it correct");
            System.out.println("Login error");
            return "welcome";
        } else {
            System.out.println("Login successful");
            // Set session if login is successful and return menu page
            session.setAttribute("accountNum", formAccount.getAccountNo());
            return "menu";
        }
    }
    
    

    /**
     * Handles GET request for /balance
     * @param model Model object which stores the balance to view
     * @param session HTTPSession object to retrieve the account number from the session
     * @return String which is the name of the HTML file to be viewed
     * 
     */
    @GetMapping("/balance")
    public String mainBalance(Model model,  HttpSession session) {
        
        float bal = accountService.getAccountByAccountNo((int)session.getAttribute("accountNum")).getBalance();
        model.addAttribute("balance", bal);    
        return "balance"; //view
    }

    /**
     * Handles GET request for /withdraw
     * @param model Model object which stores the account number to view
     * @param session HTTPSession object to retrieve the account number from the session
     * @return String which is the name of the HTML file to be viewed
     * 
     */
    @GetMapping("/withdraw")
    public String withdraw(Model model, HttpSession session) {
        model.addAttribute("account", new Account());
        int accnum = (int)session.getAttribute("accountNum");
        model.addAttribute("accnum", accnum);
        
        return "withdraw"; //view
    }

    /**
     * test function
     * Handles GET request for /accounts
     * @@param model Model object which stores the accounts to view
     * @return String which is the name of the HTML file to be viewed
     */
    @GetMapping("/accounts")
    private String getAllAccounts(Model model) {
        List<Account> accounts = accountService.getAllAccounts();
        model.addAttribute("accounts", accounts);
        return "welcome";
    }

    /**
     * Handles GET request for /menu
     * @return menu HTML file name as a String
     */

    @GetMapping("/menu")
    public String goToMenu() {
        return "menu";
    }

    /**
     * Handles GET request for /transfer
     * @param model
     * @return transfer HTML filename as a String
     */
    @GetMapping("/transfer")
    public String goToTransfer(Model model) {
        model.addAttribute("account", new Account());
        return "transfer";
    }

    /**
     * Handles GET request for /logout
     * Removes accountnumber from the session
     * @param model
     * @param session
     * @return welcome page
     */
    @GetMapping("/logout")
    public String logout(Model model,HttpSession session,HttpServletResponse httpServletResponse) {
        session.invalidate();
        model.asMap().clear();
        //session.removeAttribute("account");
        model.addAttribute("account", new Account());

        httpServletResponse.setHeader("Refresh", "100;url=shutdown");

        return "welcome";
      }

    /**
     * Handles POST request for /transfer
     * @param targetAccount the account where money is to be transferred
     * @param session gets current account number from the session
     * @param model adds messages to the view
     * @return transfer HTML filename as a String
     * 
     */
    @PostMapping("/transfer")
    public String makeTransfer(@ModelAttribute Account targetAccount, HttpSession session, Model model) {
        //calls the appropriate service function to transfer money
        boolean result = accountService.makeTransfer((Integer)session.getAttribute("accountNum"),
                targetAccount.getAccountNo(), targetAccount.getBalance());
        Transaction transaction =  transactionService.getTransaction();
        float balance = accountService.getBalance((Integer)session.getAttribute("accountNum"));
        if (result) {
            model.addAttribute("message", "Transfer was successful");
            model.addAttribute("transaction", transaction);
            model.addAttribute("accountNo", transaction.getAccountNo());
            model.addAttribute("transferred", transaction.getAmount());
            model.addAttribute("balance", balance);
        } else {
            model.addAttribute("message", "Low balance. Could not transfer. ");
        }
        return "transfer";
    }

    /**
     * Handles POST mapping for /withdraw
     * @param account
     * @param session
     * @param model
     * @return
     */
    @PostMapping("/withdraw")
    public String withdrawMoney(@ModelAttribute Account account, HttpSession session, Model model) {
        int accountNum = (Integer)session.getAttribute("accountNum");
        boolean result = accountService.withdrawMoney(accountNum,
                account.getBalance());
        float balance = accountService.getBalance(accountNum);
        if (result) {
            model.addAttribute("message", "You withdrew ₹" + account.getBalance());
            model.addAttribute("balance", balance);
            model.addAttribute("accountNum", accountNum);
        } else {
            model.addAttribute("message", "Low balance. Could not withdraw. ");
        }
        return "withdraw";
    }

    /**
     * Handles GET request for /deposit
     * @param model
     * @return
     */

    @GetMapping("/deposit")
    public String deposit(Model model) {
        model.addAttribute("account", new Account());
        return "deposit"; //view
    }

    /**
     * Hnadles POST request for /deposit
     * @param account
     * @param session
     * @param model
     * @return
     */
    @PostMapping("/deposit")
    public String depositMoney(@ModelAttribute Account account, HttpSession session, Model model) {
        int accountNum = (Integer)session.getAttribute("accountNum");
        boolean result = accountService.depositMoney(accountNum,
                account.getBalance());
        float balance = accountService.getBalance(accountNum);
        if (result) {
            model.addAttribute("message", "You deposited ₹" + account.getBalance());
            model.addAttribute("balance", balance);
            model.addAttribute("accountNum", accountNum);
        } else {
            model.addAttribute("message", "Error while depositing.");
        }
        return "deposit";
    }

    
    @GetMapping("/change_pin")
    public String showChangePinForm(Model model) {
        model.addAttribute("account", new Account());
        return "change_pin";
    }


    @PostMapping("/change_pin")
    public String processChangePinForm(@ModelAttribute Account account, HttpSession session, Model model) {
        // Retrieve the account number from the session
        int accountNum = (Integer)session.getAttribute("accountNum");
            
        // Get the new PIN from the Account object
        int newPin = account.getPin();
            
        // Call the changePin method to update the PIN in the database
        boolean result = accountService.changePin(accountNum, newPin);
            
        // Check if the PIN was successfully updated
        if (result) {
            model.addAttribute("message", "You Changed Pin");
            model.addAttribute("Pin", newPin);
            model.addAttribute("accountNum", accountNum);
        } else {
            model.addAttribute("message", "Error while changing Pin.");
        }
            
        // Return the view name for the change_pin page
        return "change_pin";
    }
    
    @GetMapping("/transactions")
    public String viewTransactions(Model model) {
    List<Transaction> transactions = transactionService.getAllTransactions();
    model.addAttribute("Transactions", transactions);
    return "transactions";
    }
    
}
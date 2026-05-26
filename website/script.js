const API_BASE = "http://localhost:3000/api/v1";

const state = {
    token: localStorage.getItem("moneymap_access_token"),
    refreshToken: localStorage.getItem("moneymap_refresh_token"),
    user: JSON.parse(localStorage.getItem("moneymap_user") || "null"),
    categories: [],
    transactions: [],
    dashboard: null,
    trends: [],
    budgets: [],
    budgetSummary: null,
    goals: [],
    subscriptions: [],
    profile: null,
    weeklyReport: null,
    monthlyReport: null,
    chatSessionId: null,
    chatMessages: []
};

const elements = {
    authView: document.getElementById("authView"),
    appView: document.getElementById("appView"),
    loginTab: document.getElementById("loginTab"),
    signupTab: document.getElementById("signupTab"),
    loginForm: document.getElementById("loginForm"),
    signupForm: document.getElementById("signupForm"),
    authMessage: document.getElementById("authMessage"),
    pageTitle: document.getElementById("pageTitle"),
    userName: document.getElementById("userName"),
    userEmail: document.getElementById("userEmail"),
    userInitials: document.getElementById("userInitials"),
    incomeMetric: document.getElementById("incomeMetric"),
    spentMetric: document.getElementById("spentMetric"),
    savingsMetric: document.getElementById("savingsMetric"),
    goalMetric: document.getElementById("goalMetric"),
    recentTransactions: document.getElementById("recentTransactions"),
    transactionList: document.getElementById("transactionList"),
    budgetList: document.getElementById("budgetList"),
    transactionForm: document.getElementById("transactionForm"),
    transactionType: document.getElementById("transactionType"),
    transactionCategory: document.getElementById("transactionCategory"),
    budgetCategory: document.getElementById("budgetCategory"),
    budgetForm: document.getElementById("budgetForm"),
    budgetAmount: document.getElementById("budgetAmount"),
    budgetMonth: document.getElementById("budgetMonth"),
    budgetYear: document.getElementById("budgetYear"),
    budgetMessage: document.getElementById("budgetMessage"),
    budgetSummaryList: document.getElementById("budgetSummaryList"),
    transactionAmount: document.getElementById("transactionAmount"),
    transactionDescription: document.getElementById("transactionDescription"),
    transactionDate: document.getElementById("transactionDate"),
    transactionMessage: document.getElementById("transactionMessage"),
    categoryForm: document.getElementById("categoryForm"),
    categoryName: document.getElementById("categoryName"),
    categoryColor: document.getElementById("categoryColor"),
    categoryMessage: document.getElementById("categoryMessage"),
    categoryList: document.getElementById("categoryList"),
    goalForm: document.getElementById("goalForm"),
    goalName: document.getElementById("goalName"),
    goalTarget: document.getElementById("goalTarget"),
    goalCurrent: document.getElementById("goalCurrent"),
    goalDate: document.getElementById("goalDate"),
    goalColor: document.getElementById("goalColor"),
    goalMessage: document.getElementById("goalMessage"),
    goalList: document.getElementById("goalList"),
    subscriptionForm: document.getElementById("subscriptionForm"),
    subscriptionName: document.getElementById("subscriptionName"),
    subscriptionAmount: document.getElementById("subscriptionAmount"),
    subscriptionCycle: document.getElementById("subscriptionCycle"),
    subscriptionDate: document.getElementById("subscriptionDate"),
    subscriptionColor: document.getElementById("subscriptionColor"),
    subscriptionMessage: document.getElementById("subscriptionMessage"),
    subscriptionList: document.getElementById("subscriptionList"),
    trendChart: document.getElementById("trendChart"),
    breakdownList: document.getElementById("breakdownList"),
    weeklyReportList: document.getElementById("weeklyReportList"),
    monthlyReportList: document.getElementById("monthlyReportList"),
    settingsForm: document.getElementById("settingsForm"),
    settingsName: document.getElementById("settingsName"),
    settingsCurrency: document.getElementById("settingsCurrency"),
    settingsNotifications: document.getElementById("settingsNotifications"),
    settingsMessage: document.getElementById("settingsMessage"),
    profileForm: document.getElementById("profileForm"),
    profileInstitution: document.getElementById("profileInstitution"),
    profileCompany: document.getElementById("profileCompany"),
    profileJobTitle: document.getElementById("profileJobTitle"),
    profileMonthlyIncome: document.getElementById("profileMonthlyIncome"),
    profileFinancialGoal: document.getElementById("profileFinancialGoal"),
    profileMessage: document.getElementById("profileMessage"),
    chatMessages: document.getElementById("chatMessages"),
    chatForm: document.getElementById("chatForm"),
    chatInput: document.getElementById("chatInput"),
    chatMessage: document.getElementById("chatMessage")
};

function money(value) {
    return new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: state.user?.currency || "USD"
    }).format(Number(value || 0));
}

function dateLabel(value) {
    if (!value) return "No date";
    return new Intl.DateTimeFormat("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric"
    }).format(new Date(value));
}

function initials(name) {
    return String(name || "MoneyMap")
        .split(" ")
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0].toUpperCase())
        .join("");
}

function setMessage(element, text, type = "error") {
    element.textContent = text || "";
    element.classList.toggle("success", type === "success");
}

async function request(path, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    if (state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers
    });

    let body = null;
    try {
        body = await response.json();
    } catch {
        body = null;
    }

    if (!response.ok) {
        const message = body?.message || body?.error || `Request failed with ${response.status}`;
        throw new Error(Array.isArray(message) ? message.join(", ") : message);
    }

    return body?.data ?? body;
}

function saveSession(payload) {
    state.token = payload.accessToken;
    state.refreshToken = payload.refreshToken;
    state.user = payload.user;
    localStorage.setItem("moneymap_access_token", payload.accessToken);
    localStorage.setItem("moneymap_refresh_token", payload.refreshToken);
    localStorage.setItem("moneymap_user", JSON.stringify(payload.user));
}

function clearSession() {
    state.token = null;
    state.refreshToken = null;
    state.user = null;
    localStorage.removeItem("moneymap_access_token");
    localStorage.removeItem("moneymap_refresh_token");
    localStorage.removeItem("moneymap_user");
}

function showApp() {
    elements.authView.classList.add("hidden");
    elements.appView.classList.remove("hidden");
    elements.userName.textContent = state.user?.name || "MoneyMap user";
    elements.userEmail.textContent = state.user?.email || "";
    elements.userInitials.textContent = initials(state.user?.name);
}

function showAuth() {
    elements.appView.classList.add("hidden");
    elements.authView.classList.remove("hidden");
}

function switchAuth(mode) {
    const signup = mode === "signup";
    elements.signupForm.classList.toggle("hidden", !signup);
    elements.loginForm.classList.toggle("hidden", signup);
    elements.signupTab.classList.toggle("active", signup);
    elements.loginTab.classList.toggle("active", !signup);
    setMessage(elements.authMessage, "");
}

async function loadAppData() {
    const [
        dashboard,
        categories,
        transactions,
        trends,
        monthly,
        weekly,
        budgets,
        budgetSummary,
        goals,
        subscriptions,
        profile
    ] = await Promise.all([
        request("/reports/dashboard"),
        request("/categories"),
        request("/transactions?limit=20"),
        request("/reports/trends"),
        request("/reports/monthly"),
        request("/reports/weekly"),
        request("/budgets"),
        request("/budgets/summary"),
        request("/savings-goals"),
        request("/subscriptions"),
        request("/users/profile")
    ]);

    state.dashboard = dashboard;
    state.categories = categories || [];
    state.transactions = transactions?.transactions || [];
    state.trends = trends || [];
    state.monthlyReport = monthly;
    state.weeklyReport = weekly;
    state.budgets = budgets || [];
    state.budgetSummary = budgetSummary;
    state.goals = goals || [];
    state.subscriptions = subscriptions || [];
    state.profile = profile;

    renderDashboard();
    renderCategories();
    renderTransactions();
    renderBudgetSummary();
    renderGoals();
    renderSubscriptions();
    renderProfile();
    renderTrends(monthly?.breakdown || []);
    renderReports();
}

function renderDashboard() {
    const data = state.dashboard || {};
    elements.incomeMetric.textContent = money(data.monthlyIncome);
    elements.spentMetric.textContent = money(data.monthlySpent);
    elements.savingsMetric.textContent = money(data.netSavings);
    elements.goalMetric.textContent = `${Math.round(data.savingsOverview?.progressPercentage || 0)}%`;

    renderTransactionList(elements.recentTransactions, data.recentTransactions || [], true);
    renderBudgets(data.budgets || []);
}

function renderTransactionList(container, transactions, compact = false) {
    if (!transactions.length) {
        container.innerHTML = `<div class="empty-state">No transactions yet. Add your first income or expense.</div>`;
        return;
    }

    container.innerHTML = transactions.map((transaction) => {
        const isIncome = transaction.type === "INCOME";
        const color = transaction.color || transaction.category?.color || (isIncome ? "#059669" : "#2563eb");
        const category = transaction.category?.name || transaction.category || "General";
        const description = transaction.description || category;
        const amountClass = isIncome ? "income" : "expense";
        const sign = isIncome ? "+" : "-";
        const when = dateLabel(transaction.transactionDate);

        return `
            <article class="transaction-item">
                <span class="transaction-icon" style="background:${color}">${category[0] || "T"}</span>
                <div>
                    <strong>${description}</strong>
                    <small>${category}${compact ? "" : ` · ${when}`}</small>
                    ${compact ? "" : `<div class="item-actions"><button class="danger-button" type="button" data-tx-delete="${transaction.id}">Delete</button></div>`}
                </div>
                <span class="amount ${amountClass}">${sign}${money(transaction.amount)}</span>
            </article>
        `;
    }).join("");
}

function renderTransactions() {
    renderTransactionList(elements.transactionList, state.transactions);
}

function renderBudgetSummary() {
    const summary = state.budgetSummary;
    const budgets = state.budgets || [];

    if (!summary || !budgets.length) {
        elements.budgetSummaryList.innerHTML = `<div class="empty-state">No budgets set for this month yet.</div>`;
        return;
    }

    const utilization = Math.round(summary.overallUtilization || 0);
    elements.budgetSummaryList.innerHTML = `
        <article class="budget-item">
            <div class="budget-head">
                <span>Total budgeted</span>
                <span>${money(summary.totalBudgeted)}</span>
            </div>
            <small>Spent ${money(summary.totalSpent)} · Remaining ${money(summary.remainingBudget)}</small>
            <div class="progress-track">
                <div class="progress-fill" style="width:${Math.min(100, utilization)}%"></div>
            </div>
        </article>
        ${budgets.map((budget) => {
            const percent = Math.min(100, Math.round(budget.utilizationPercentage || 0));
            return `
                <article class="budget-item">
                    <div class="budget-head">
                        <span>${budget.categoryName}</span>
                        <span>${percent}%</span>
                    </div>
                    <div class="progress-track">
                        <div class="progress-fill" style="width:${percent}%;background:${budget.color || "#2563eb"}"></div>
                    </div>
                    <small>${money(budget.spent)} of ${money(budget.limit)}</small>
                </article>
            `;
        }).join("")}
    `;
}

function renderBudgets(budgets) {
    if (!budgets.length) {
        elements.budgetList.innerHTML = `<div class="empty-state">No budgets yet. Your category spending will appear here.</div>`;
        return;
    }

    elements.budgetList.innerHTML = budgets.map((budget) => {
        const percent = Math.min(100, Math.round(budget.utilizationPercentage || 0));
        return `
            <article class="budget-item">
                <div class="budget-head">
                    <span>${budget.categoryName}</span>
                    <span>${percent}%</span>
                </div>
                <div class="progress-track">
                    <div class="progress-fill" style="width:${percent}%;background:${budget.color || "#2563eb"}"></div>
                </div>
                <small>${money(budget.spent)} of ${money(budget.limit)}</small>
            </article>
        `;
    }).join("");
}

function renderCategories() {
    if (!state.categories.length) {
        elements.categoryList.innerHTML = `<div class="empty-state">No categories found. Create one to start adding transactions.</div>`;
        elements.transactionCategory.innerHTML = `<option value="">Create a category first</option>`;
        elements.budgetCategory.innerHTML = `<option value="">Create a category first</option>`;
        return;
    }

    elements.categoryList.innerHTML = state.categories.map((category) => `
        <article class="category-item">
            <span class="color-dot" style="background:${category.color}"></span>
            ${category.name}
        </article>
    `).join("");

    elements.transactionCategory.innerHTML = state.categories.map((category) => `
        <option value="${category.id}">${category.name}</option>
    `).join("");

    elements.budgetCategory.innerHTML = state.categories.map((category) => `
        <option value="${category.id}">${category.name}</option>
    `).join("");
}

function renderGoals() {
    if (!state.goals.length) {
        elements.goalList.innerHTML = `<div class="empty-state">No savings goals yet. Add a goal to track progress.</div>`;
        return;
    }

    elements.goalList.innerHTML = state.goals.map((goal) => {
        const percent = Math.min(100, Math.round(goal.progressPercentage || 0));
        return `
            <article class="budget-item">
                <div class="budget-head">
                    <span>${escapeHtml(goal.name)}</span>
                    <span>${percent}%</span>
                </div>
                <div class="progress-track">
                    <div class="progress-fill" style="width:${percent}%;background:${goal.color || "#059669"}"></div>
                </div>
                <small>${money(goal.currentAmount)} of ${money(goal.targetAmount)}${goal.targetDate ? ` · ${dateLabel(goal.targetDate)}` : ""}</small>
                <div class="item-actions">
                    <button class="small-button" type="button" data-goal-add="${goal.id}">Add ${money(50)}</button>
                    <button class="danger-button" type="button" data-goal-delete="${goal.id}">Delete</button>
                </div>
            </article>
        `;
    }).join("");
}

function renderSubscriptions() {
    if (!state.subscriptions.length) {
        elements.subscriptionList.innerHTML = `<div class="empty-state">No subscriptions yet. Add recurring bills to track upcoming payments.</div>`;
        return;
    }

    elements.subscriptionList.innerHTML = state.subscriptions.map((sub) => `
        <article class="budget-item">
            <div class="budget-head">
                <span>${escapeHtml(sub.name)}</span>
                <span>${money(sub.amount)}</span>
            </div>
            <small>${sub.billingCycle} · Next billing ${dateLabel(sub.nextBillingDate)} · ${sub.isActive ? "Active" : "Paused"}</small>
            <div class="item-actions">
                <button class="small-button" type="button" data-sub-toggle="${sub.id}" data-active="${sub.isActive}">${sub.isActive ? "Pause" : "Activate"}</button>
                <button class="danger-button" type="button" data-sub-delete="${sub.id}">Delete</button>
            </div>
        </article>
    `).join("");
}

function renderProfile() {
    if (!state.profile) return;

    const profile = state.profile.profile || {};
    elements.settingsName.value = state.profile.name || "";
    elements.settingsCurrency.value = state.profile.currency || "USD";
    elements.settingsNotifications.checked = Boolean(profile.notificationsEnabled);
    elements.profileInstitution.value = profile.institution || "";
    elements.profileCompany.value = profile.companyName || "";
    elements.profileJobTitle.value = profile.jobTitle || "";
    elements.profileMonthlyIncome.value = profile.monthlyIncome ? Number(profile.monthlyIncome) : "";
    elements.profileFinancialGoal.value = profile.financialGoal || "";
}

function renderReports() {
    const weekly = state.weeklyReport;
    const monthly = state.monthlyReport;

    if (!weekly) {
        elements.weeklyReportList.innerHTML = `<div class="empty-state">Weekly report unavailable.</div>`;
    } else {
        elements.weeklyReportList.innerHTML = `
            <article class="budget-item">
                <div class="budget-head"><span>Total spent</span><span>${money(weekly.totalSpent)}</span></div>
                <small>Daily average ${money(weekly.averageDailySpent)}</small>
            </article>
            ${(weekly.breakdown || []).slice(0, 4).map((item) => `
                <article class="budget-item">
                    <div class="budget-head"><span>${item.category}</span><span>${Math.round(item.percentage)}%</span></div>
                    <small>${money(item.amount)}</small>
                </article>
            `).join("")}
        `;
    }

    if (!monthly) {
        elements.monthlyReportList.innerHTML = `<div class="empty-state">Monthly report unavailable.</div>`;
    } else {
        elements.monthlyReportList.innerHTML = `
            <article class="budget-item">
                <div class="budget-head"><span>This month</span><span>${money(monthly.totalSpent)}</span></div>
                <small>Previous month ${money(monthly.previousMonthSpent)} · Change ${Math.round(monthly.percentageChange || 0)}%</small>
            </article>
        `;
    }
}

function renderTrends(breakdown) {
    if (!state.trends.length) {
        elements.trendChart.innerHTML = `<div class="empty-state">Trend data will appear after transactions are recorded.</div>`;
    } else {
        const max = Math.max(...state.trends.map((item) => item.income || item.expenses || 1), 1);
        elements.trendChart.innerHTML = state.trends.map((item) => {
            const height = Math.max(18, Math.round(((item.expenses || item.income || 0) / max) * 220));
            return `
                <div class="trend-bar">
                    <span style="height:${height}px"></span>
                    <small>${item.monthName}</small>
                </div>
            `;
        }).join("");
    }

    if (!breakdown.length) {
        elements.breakdownList.innerHTML = `<div class="empty-state">No monthly spending breakdown yet.</div>`;
        return;
    }

    elements.breakdownList.innerHTML = breakdown.map((item) => `
        <article class="budget-item">
            <div class="budget-head">
                <span>${item.category}</span>
                <span>${Math.round(item.percentage)}%</span>
            </div>
            <div class="progress-track">
                <div class="progress-fill" style="width:${Math.min(100, item.percentage)}%;background:${item.color || "#2563eb"}"></div>
            </div>
            <small>${money(item.amount)}</small>
        </article>
    `).join("");
}

function renderChat() {
    if (!state.chatMessages.length) {
        elements.chatMessages.innerHTML = `
            <div class="empty-state">
                Ask the assistant about your spending, budget limits, savings goals, or simple ways to improve your month.
            </div>
        `;
        return;
    }

    elements.chatMessages.innerHTML = state.chatMessages.map((message) => `
        <div class="chat-bubble ${message.isUser ? "user" : "assistant"}">${escapeHtml(message.content)}</div>
    `).join("");
    elements.chatMessages.scrollTop = elements.chatMessages.scrollHeight;
}

function escapeHtml(value) {
    return String(value || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

async function sendAssistantMessage(content) {
    const text = content.trim();
    if (!text) return;

    setMessage(elements.chatMessage, "Thinking...", "success");
    state.chatMessages.push({ isUser: true, content: text });
    renderChat();
    elements.chatInput.value = "";

    try {
        const response = await request("/chatbot/message", {
            method: "POST",
            body: JSON.stringify({
                sessionId: state.chatSessionId || undefined,
                content: text
            })
        });

        state.chatSessionId = response.sessionId;
        state.chatMessages.push({
            isUser: false,
            content: response.message?.content || "I could not generate a response right now."
        });
        setMessage(elements.chatMessage, "");
        renderChat();
    } catch (error) {
        state.chatMessages.push({
            isUser: false,
            content: "I could not reach the assistant service. Check that the backend is running."
        });
        setMessage(elements.chatMessage, error.message);
        renderChat();
    }
}

function switchView(viewName) {
    document.querySelectorAll(".view-section").forEach((section) => {
        section.classList.add("hidden");
    });
    document.getElementById(`${viewName}View`).classList.remove("hidden");

    document.querySelectorAll(".nav-item").forEach((item) => {
        item.classList.toggle("active", item.dataset.view === viewName);
    });

    elements.pageTitle.textContent = viewName[0].toUpperCase() + viewName.slice(1);
}

async function ensureStarterCategories() {
    if (state.categories.length) return;

    const starters = [
        { name: "Food", color: "#f97316", icon: "food" },
        { name: "Transport", color: "#2563eb", icon: "car" },
        { name: "Salary", color: "#059669", icon: "salary" },
        { name: "Shopping", color: "#db2777", icon: "shopping" }
    ];

    for (const category of starters) {
        try {
            await request("/categories", {
                method: "POST",
                body: JSON.stringify(category)
            });
        } catch {
            // Existing categories are fine; reload below.
        }
    }

    state.categories = await request("/categories");
}

elements.loginTab.addEventListener("click", () => switchAuth("login"));
elements.signupTab.addEventListener("click", () => switchAuth("signup"));

elements.loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.authMessage, "Logging in...", "success");

    try {
        const payload = await request("/auth/login", {
            method: "POST",
            body: JSON.stringify({
                email: document.getElementById("loginEmail").value.trim(),
                password: document.getElementById("loginPassword").value
            })
        });
        saveSession(payload);
        showApp();
        await loadAppData();
        await ensureStarterCategories();
        renderCategories();
    } catch (error) {
        setMessage(elements.authMessage, error.message);
    }
});

elements.signupForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.authMessage, "Creating account...", "success");

    try {
        const email = document.getElementById("signupEmail").value.trim();
        const password = document.getElementById("signupPassword").value;
        await request("/auth/signup", {
            method: "POST",
            body: JSON.stringify({
                name: document.getElementById("signupName").value.trim(),
                email,
                password,
                role: "PERSONAL",
                currency: "USD"
            })
        });

        const payload = await request("/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password })
        });
        saveSession(payload);
        showApp();
        await loadAppData();
        await ensureStarterCategories();
        renderCategories();
    } catch (error) {
        setMessage(elements.authMessage, error.message);
    }
});

elements.transactionForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.transactionMessage, "Saving transaction...", "success");

    try {
        if (!elements.transactionCategory.value) {
            throw new Error("Create a category before adding a transaction.");
        }

        await request("/transactions", {
            method: "POST",
            body: JSON.stringify({
                type: elements.transactionType.value,
                categoryId: elements.transactionCategory.value,
                amount: Number(elements.transactionAmount.value),
                description: elements.transactionDescription.value.trim(),
                transactionDate: elements.transactionDate.value,
                tags: []
            })
        });

        elements.transactionForm.reset();
        elements.transactionDate.valueAsDate = new Date();
        setMessage(elements.transactionMessage, "Transaction saved.", "success");
        await loadAppData();
    } catch (error) {
        setMessage(elements.transactionMessage, error.message);
    }
});

elements.categoryForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.categoryMessage, "Adding category...", "success");

    try {
        await request("/categories", {
            method: "POST",
            body: JSON.stringify({
                name: elements.categoryName.value.trim(),
                color: elements.categoryColor.value,
                icon: elements.categoryName.value.trim().toLowerCase() || "category"
            })
        });

        elements.categoryForm.reset();
        elements.categoryColor.value = "#2563eb";
        setMessage(elements.categoryMessage, "Category added.", "success");
        state.categories = await request("/categories");
        renderCategories();
    } catch (error) {
        setMessage(elements.categoryMessage, error.message);
    }
});

elements.budgetForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.budgetMessage, "Saving budget...", "success");

    try {
        if (!elements.budgetCategory.value) {
            throw new Error("Create a category before setting a budget.");
        }

        await request("/budgets", {
            method: "POST",
            body: JSON.stringify({
                categoryId: elements.budgetCategory.value,
                amount: Number(elements.budgetAmount.value),
                month: Number(elements.budgetMonth.value),
                year: Number(elements.budgetYear.value)
            })
        });

        elements.budgetAmount.value = "";
        setMessage(elements.budgetMessage, "Budget saved.", "success");
        await loadAppData();
    } catch (error) {
        setMessage(elements.budgetMessage, error.message);
    }
});

elements.goalForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.goalMessage, "Saving goal...", "success");

    try {
        await request("/savings-goals", {
            method: "POST",
            body: JSON.stringify({
                name: elements.goalName.value.trim(),
                targetAmount: Number(elements.goalTarget.value),
                currentAmount: Number(elements.goalCurrent.value || 0),
                targetDate: elements.goalDate.value || undefined,
                icon: "flag",
                color: elements.goalColor.value
            })
        });

        elements.goalForm.reset();
        elements.goalCurrent.value = "0";
        elements.goalColor.value = "#059669";
        setMessage(elements.goalMessage, "Goal saved.", "success");
        await loadAppData();
    } catch (error) {
        setMessage(elements.goalMessage, error.message);
    }
});

elements.subscriptionForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.subscriptionMessage, "Saving subscription...", "success");

    try {
        await request("/subscriptions", {
            method: "POST",
            body: JSON.stringify({
                name: elements.subscriptionName.value.trim(),
                amount: Number(elements.subscriptionAmount.value),
                billingCycle: elements.subscriptionCycle.value,
                nextBillingDate: elements.subscriptionDate.value,
                color: elements.subscriptionColor.value,
                isActive: true
            })
        });

        elements.subscriptionForm.reset();
        elements.subscriptionCycle.value = "MONTHLY";
        elements.subscriptionColor.value = "#db2777";
        elements.subscriptionDate.valueAsDate = new Date();
        setMessage(elements.subscriptionMessage, "Subscription saved.", "success");
        await loadAppData();
    } catch (error) {
        setMessage(elements.subscriptionMessage, error.message);
    }
});

elements.settingsForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.settingsMessage, "Saving settings...", "success");

    try {
        state.profile = await request("/users/settings", {
            method: "PATCH",
            body: JSON.stringify({
                name: elements.settingsName.value.trim(),
                currency: elements.settingsCurrency.value,
                notificationsEnabled: elements.settingsNotifications.checked,
                budgetAlerts: elements.settingsNotifications.checked,
                goalReminders: elements.settingsNotifications.checked,
                subscriptionReminders: elements.settingsNotifications.checked,
                weeklyReport: elements.settingsNotifications.checked,
                monthlyReport: elements.settingsNotifications.checked
            })
        });

        state.user = {
            ...state.user,
            name: state.profile.name,
            currency: state.profile.currency
        };
        localStorage.setItem("moneymap_user", JSON.stringify(state.user));
        showApp();
        setMessage(elements.settingsMessage, "Settings saved.", "success");
    } catch (error) {
        setMessage(elements.settingsMessage, error.message);
    }
});

elements.profileForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(elements.profileMessage, "Saving profile...", "success");

    try {
        await request("/users/profile", {
            method: "PATCH",
            body: JSON.stringify({
                institution: elements.profileInstitution.value.trim() || undefined,
                companyName: elements.profileCompany.value.trim() || undefined,
                jobTitle: elements.profileJobTitle.value.trim() || undefined,
                monthlyIncome: elements.profileMonthlyIncome.value ? Number(elements.profileMonthlyIncome.value) : undefined,
                financialGoal: elements.profileFinancialGoal.value.trim() || undefined,
                onboardingCompleted: true
            })
        });

        state.profile = await request("/users/profile");
        renderProfile();
        setMessage(elements.profileMessage, "Profile saved.", "success");
    } catch (error) {
        setMessage(elements.profileMessage, error.message);
    }
});

document.addEventListener("click", async (event) => {
    const txDelete = event.target.closest("[data-tx-delete]");
    const goalAdd = event.target.closest("[data-goal-add]");
    const goalDelete = event.target.closest("[data-goal-delete]");
    const subToggle = event.target.closest("[data-sub-toggle]");
    const subDelete = event.target.closest("[data-sub-delete]");

    try {
        if (txDelete) {
            await request(`/transactions/${txDelete.dataset.txDelete}`, { method: "DELETE" });
            await loadAppData();
        }

        if (goalAdd) {
            const goal = state.goals.find((item) => item.id === goalAdd.dataset.goalAdd);
            if (goal) {
                await request(`/savings-goals/${goal.id}`, {
                    method: "PATCH",
                    body: JSON.stringify({
                        currentAmount: Number(goal.currentAmount || 0) + 50
                    })
                });
                await loadAppData();
            }
        }

        if (goalDelete) {
            await request(`/savings-goals/${goalDelete.dataset.goalDelete}`, { method: "DELETE" });
            await loadAppData();
        }

        if (subToggle) {
            await request(`/subscriptions/${subToggle.dataset.subToggle}`, {
                method: "PATCH",
                body: JSON.stringify({ isActive: subToggle.dataset.active !== "true" })
            });
            await loadAppData();
        }

        if (subDelete) {
            await request(`/subscriptions/${subDelete.dataset.subDelete}`, { method: "DELETE" });
            await loadAppData();
        }
    } catch (error) {
        console.error(error);
    }
});

document.querySelectorAll(".nav-item").forEach((button) => {
    button.addEventListener("click", () => switchView(button.dataset.view));
});

document.querySelectorAll("[data-view-link]").forEach((button) => {
    button.addEventListener("click", () => switchView(button.dataset.viewLink));
});

document.getElementById("refreshButton").addEventListener("click", loadAppData);

elements.chatForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await sendAssistantMessage(elements.chatInput.value);
});

document.getElementById("newChatButton").addEventListener("click", () => {
    state.chatSessionId = null;
    state.chatMessages = [];
    setMessage(elements.chatMessage, "");
    renderChat();
});

document.querySelectorAll(".prompt-chip").forEach((button) => {
    button.addEventListener("click", async () => {
        switchView("assistant");
        await sendAssistantMessage(button.textContent);
    });
});

document.getElementById("logoutButton").addEventListener("click", () => {
    clearSession();
    state.chatSessionId = null;
    state.chatMessages = [];
    showAuth();
});

elements.transactionDate.valueAsDate = new Date();
elements.subscriptionDate.valueAsDate = new Date();
elements.budgetMonth.value = String(new Date().getMonth() + 1);
elements.budgetYear.value = String(new Date().getFullYear());
renderChat();

if (state.token && state.user) {
    showApp();
    loadAppData()
        .then(ensureStarterCategories)
        .then(renderCategories)
        .catch((error) => {
            clearSession();
            showAuth();
            setMessage(elements.authMessage, error.message);
        });
} else {
    showAuth();
}

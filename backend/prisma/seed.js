"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g = Object.create((typeof Iterator === "function" ? Iterator : Object).prototype);
    return g.next = verb(0), g["throw"] = verb(1), g["return"] = verb(2), typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
Object.defineProperty(exports, "__esModule", { value: true });
var client_1 = require("@prisma/client");
var prisma = new client_1.PrismaClient();
var SYSTEM_CATEGORIES = [
    { name: 'Food', color: '#FF7A00', icon: 'restaurant' },
    { name: 'Transport', color: '#3B82F6', icon: 'directions_car' },
    { name: 'Shopping', color: '#EC4899', icon: 'shopping_bag' },
    { name: 'Bills', color: '#EAB308', icon: 'flash_on' },
    { name: 'Entertainment', color: '#A855F7', icon: 'movie' },
    { name: 'Health', color: '#EF4444', icon: 'medical_services' },
    { name: 'Education', color: '#6366F1', icon: 'school' },
    { name: 'Groceries', color: '#22C55E', icon: 'local_grocery_store' },
    { name: 'Utilities', color: '#14B8A6', icon: 'bolt' },
    { name: 'Healthcare', color: '#F43F5E', icon: 'health_and_safety' },
    { name: 'Income', color: '#10B981', icon: 'account_balance' },
    { name: 'Other', color: '#64748B', icon: 'more_horiz' },
];
function main() {
    return __awaiter(this, void 0, void 0, function () {
        var _i, SYSTEM_CATEGORIES_1, cat;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    console.log('🌱 Seeding database...');
                    _i = 0, SYSTEM_CATEGORIES_1 = SYSTEM_CATEGORIES;
                    _a.label = 1;
                case 1:
                    if (!(_i < SYSTEM_CATEGORIES_1.length)) return [3 /*break*/, 4];
                    cat = SYSTEM_CATEGORIES_1[_i];
                    return [4 /*yield*/, prisma.category.upsert({
                            where: {
                                name_userId: {
                                    name: cat.name,
                                    userId: '00000000-0000-0000-0000-000000000000', // system sentinel — won't match any real user
                                },
                            },
                            update: {},
                            create: {
                                name: cat.name,
                                color: cat.color,
                                icon: cat.icon,
                                isSystem: true,
                                userId: null,
                            },
                        })];
                case 2:
                    _a.sent();
                    _a.label = 3;
                case 3:
                    _i++;
                    return [3 /*break*/, 1];
                case 4:
                    console.log("\u2705 Created ".concat(SYSTEM_CATEGORIES.length, " system categories"));
                    console.log('🌱 Seeding complete!');
                    return [2 /*return*/];
            }
        });
    });
}
main()
    .catch(function (e) {
    console.error('❌ Seed error:', e);
    process.exit(1);
})
    .finally(function () { return __awaiter(void 0, void 0, void 0, function () {
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4 /*yield*/, prisma.$disconnect()];
            case 1:
                _a.sent();
                return [2 /*return*/];
        }
    });
}); });

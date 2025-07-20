/*
I am solving this problem to learn Observer design Pattern
Problem statement: building a StockTicker backend service.

Requirements:

There is a StockMarket system that keeps updating stock prices (AAPL, GOOGL, etc.).
Multiple clients are interested in certain stocks:
A MobileTraderApp wants updates on AAPL and GOOGL.
A NewsAgencyFeed subscribes to all major stocks.
A DataLoggerService logs price changes for analysis.
Every time a stock price changes, all subscribers interested in that stock must be notified.
Subscribers should be able to unsubscribe anytime.
Design must support adding new observers later with minimal changes.
*/

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

class StockMarketMain {
    public static void main(String[] args) {
        StockMarket market = new StockMarket(new HashMap<>(), new StockFactory());

        market.addStock("AAPL", 150);
        market.addStock("GOOGL", 135);
        market.addStock("TSLA", 689);

        Observer<Stock> mobileApp = new MobileTraderApp();
        Observer<Stock> logger = new DataLoggerService();
        Observer<Stock> news = new NewsAgencyFeed();

        market.registerObserver("AAPL", mobileApp);
        market.registerObserver("AAPL", logger);
        market.registerObserver("GOOGL", mobileApp);
        market.registerObserver("TSLA", news);

        market.updateStock("AAPL", 198);
        market.updateStock("TSLA", 812);
    }
}

interface Observer<T> {
    void update(T data);
}

interface Subject<T> {
    void subscribe(Observer<T> observer);
    void unsubscribe(Observer<T> observer);
    void notifySubscribers(T data);
}

abstract class AbstractSubject<T> implements Subject<T> {
    private final List<Observer<T>> observers = new CopyOnWriteArrayList<>();
    
    @Override
    public void subscribe(Observer<T> observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribe(Observer<T> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifySubscribers(T data) {
        for (Observer<T> observer : observers) {
            observer.update(data);
        }
    }
}

class Stock extends AbstractSubject<Stock> {
    private String name;
    private int price;

    public Stock(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return this.name;
    }

    public int getPrice() {
        return this.price;
    }

    public void changePrice(int price) {
        this.price = price;
        this.notifySubscribers(this);
    }
}

class StockFactory {
    public Stock generateNewStock(String name, int price) {
        return new Stock(name, price);
    }
}

class StockMarket {
    Map<String, Stock> stocksList;
    StockFactory stockFactory;

    public StockMarket(Map<String, Stock> stocksList, StockFactory stockFactory) {
        this.stocksList = stocksList;
        this.stockFactory = stockFactory;
    }

    public void addStock(String name, int price) {
        Stock newStock = this.stockFactory.generateNewStock(name, price);
        this.stocksList.put(name, newStock);
    }

    public void updateStock(String name, int price) {
        if (this.stocksList.containsKey(name)) {
            Stock updatedStock = this.stocksList.get(name);
            updatedStock.changePrice(price);
            this.stocksList.put(name, updatedStock);
        } else {
            System.out.println("This stock is not listed on the market");
        }
    }

    public void registerObserver(String name, Observer<Stock> observer) {
        if (this.stocksList.containsKey(name)) {
            this.stocksList.get(name).subscribe(observer);
        } else {
            System.out.println("This stock is not listed on the market");
        }
    }
}

class MobileTraderApp implements Observer<Stock> {

    @Override
    public void update(Stock stock) {
        System.out.println(stock.getName() + "\'s price has been changed to " + stock.getPrice());

        // do whatever needed to be done on price change
    }
}

class NewsAgencyFeed implements Observer<Stock> {

    @Override
    public void update(Stock stock) {
        System.out.println(stock.getName() + "\'s price has been changed to " + stock.getPrice());

        // do whatever needed to be done on price change
    }
}

class DataLoggerService implements Observer<Stock> {

    @Override
    public void update(Stock stock) {
        System.out.println(stock.getName() + "\'s price has been changed to " + stock.getPrice());

        // do whatever needed to be done on price change
    }
}
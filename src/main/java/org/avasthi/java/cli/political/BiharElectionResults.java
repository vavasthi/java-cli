package org.avasthi.java.cli.political;

import com.mongodb.client.MongoCollection;
import org.avasthi.java.cli.Base;
import org.avasthi.java.cli.political.pojos.ElectionType;
import org.avasthi.java.cli.political.pojos.RoundWiseCount;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BiharElectionResults extends Base {
    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        BiharElectionResults ber = new BiharElectionResults();
        ber.generateCsv();
//        ber.parseResults();
    }

    private void generateCsv() throws FileNotFoundException {

        PrintWriter csv = new PrintWriter(new File("bihar-result.csv"));
        csv.println("Candidate, Party, Constituency Number, Constituency, round, totalRounds, Votes Previous Round, Votes Current Round, Total");
        MongoCollection<RoundWiseCount> collection = getMongoClient().getDatabase("Bihar").getCollection("RoundWiseCount", RoundWiseCount.class);
        for (RoundWiseCount rwc: collection.find()) {

            csv.println(String.format("%s,%s,%d,%s,%d,%d,%d,%d,%d", rwc.candidate(), rwc.party(), rwc.constituencyNumber(), rwc.constituencyName(), rwc.round(), rwc.totalRounds(), rwc.votesFromPreviousRounds(), rwc.votesCurrentRound(), rwc.totalVotes()));
        }
        csv.close();
    }
    private void parseResults() throws IOException {


        PrintWriter csv = new PrintWriter(new File("bihar-result.csv"));
        csv.println("Sl No,Candidate, Party, Constituency Number, Constituency, round, totalRounds, Votes Previous Round, Votes Current Round, Total");
        MongoCollection<RoundWiseCount> collection = getMongoClient().getDatabase("Bihar").getCollection("RoundWiseCount", RoundWiseCount.class);
        for (int i=243;i<244;++i) {
            parseSingleConstituency(i, csv, collection);
        }
        csv.close();
    }
    void parseSingleConstituency(int constituencyNumber, PrintWriter csv, MongoCollection<RoundWiseCount> collection) throws IOException {

        List<RoundWiseCount> roundWiseCountList = new ArrayList<>();
        WebDriver driver = getWebDriver();
        String url = String.format("file:///Users/vavasthi/data/bihar/rw-%d.html",constituencyNumber);
        driver.get(url);
        WebElement constituency = driver.findElement(By.xpath("/html/body/main/div/div[1]/h2/span"));
        System.out.println(constituency.getText());
        int rounds = Integer.parseInt(driver.findElement(By.cssSelector("div.round-status")).getText().split("/")[1]);
        int carouselFlips = 1;
        int carouselEntry = 1;
        for (int i = 1; i <= rounds; ++i) {
            if (carouselEntry > 17) {
                carouselFlips += 1;
                carouselEntry = 1;
            }
            if (i == 35) {
                carouselFlips = 2;
                carouselEntry = 18;
            }
            String buttonXpath = String.format("/html/body/main/div/div[4]/div/div/div/div/div/div[%d]/ul/li[%d]/button", carouselFlips, carouselEntry);
            if (rounds == 36 && i == 36) {
                buttonXpath = String.format("/html/body/main/div/div[4]/div/div/div/div/div/div[%d]/ul/li/button", carouselFlips);
            }
            WebElement button = driver.findElement(By.xpath(buttonXpath));
            button.click();
            for (int j = 1; j < 5000; ++j) {
                try {
                    String nameXpath = String.format("/html/body/main/div/div[%d]/div/table/tbody/tr[%d]/td[1]", i+4, j);
                    String partyXpath = String.format("/html/body/main/div/div[%d]/div/table/tbody/tr[%d]/td[2]", i+4, j);
                    String votesPreviousRoundXpath = String.format("/html/body/main/div/div[%d]/div/table/tbody/tr[%d]/td[3]", i+4, j);
                    String currentRoundXpath = String.format("/html/body/main/div/div[%d]/div/table/tbody/tr[%d]/td[4]", i+4, j);
                    String totalXpath = String.format("/html/body/main/div/div[%d]/div/table/tbody/tr[%d]/td[5]", i+4, j);
                    WebElement we = driver.findElement(By.xpath(nameXpath));
                    String name = driver.findElement(By.xpath(nameXpath)).getText().replaceAll(",", "-");
                    String party = driver.findElement(By.xpath(partyXpath)).getText().replaceAll(",", "-");
                    long votePreviousRound = Long.parseLong(driver.findElement(By.xpath(votesPreviousRoundXpath)).getText());
                    long currentRound = Long.parseLong(driver.findElement(By.xpath(currentRoundXpath)).getText());
                    long total = Long.parseLong(driver.findElement(By.xpath(totalXpath)).getText());
                    csv.println(String.format("%d,%s,%s,%d,%s,%d,%d,%d,%d,%d", j, name, party, constituencyNumber, constituency.getText(), i, rounds, votePreviousRound, currentRound, total));
                    RoundWiseCount rwc = new RoundWiseCount(UUID.randomUUID(), ElectionType.ASSEMBLY_GENERAL, constituencyNumber, constituency.getText(), i, rounds, name, party, votePreviousRound, currentRound, total);
                    roundWiseCountList.add(rwc);
                    if (roundWiseCountList.size() > 500) {
                        collection.insertMany(roundWiseCountList);
                        roundWiseCountList.clear();
                    }
                }
                catch (NumberFormatException nex) {
                    System.out.println(String.format("Number format exception for constituency = %d, round = %d",constituencyNumber, i));
                    nex.printStackTrace();
                    throw nex;
                }
                catch (NoSuchElementException e) {
                    break;
                }

            }

            ++carouselEntry;
        }
        if (roundWiseCountList.size() > 0) {

            collection.insertMany(roundWiseCountList);
        }
        driver.close();
    }
}
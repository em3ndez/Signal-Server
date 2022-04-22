package org.whispersystems.textsecuregcm.currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.whispersystems.textsecuregcm.entities.CurrencyConversionEntityList;

class CurrencyConversionManagerTest {

  @Test
  void testCurrencyCalculations() throws IOException {
    FixerClient fixerClient = mock(FixerClient.class);
    FtxClient   ftxClient   = mock(FtxClient.class);

    when(ftxClient.getSpotPrice(eq("FOO"), eq("USD"))).thenReturn(new BigDecimal("2.35"));
    when(fixerClient.getConversionsForBase(eq("USD"))).thenReturn(Map.of(
        "EUR", new BigDecimal("0.822876"),
        "FJD", new BigDecimal("2.0577"),
        "FKP", new BigDecimal("0.743446")
    ));

    CurrencyConversionManager manager = new CurrencyConversionManager(fixerClient, ftxClient, List.of("FOO"));

    manager.updateCacheIfNecessary();

    CurrencyConversionEntityList conversions = manager.getCurrencyConversions().orElseThrow();

    assertThat(conversions.getCurrencies().size()).isEqualTo(1);
    assertThat(conversions.getCurrencies().get(0).getBase()).isEqualTo("FOO");
    assertThat(conversions.getCurrencies().get(0).getConversions().size()).isEqualTo(4);
    assertThat(conversions.getCurrencies().get(0).getConversions().get("USD")).isEqualTo(new BigDecimal("2.35"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("EUR")).isEqualTo(new BigDecimal("1.9337586"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("FJD")).isEqualTo(new BigDecimal("4.835595"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("FKP")).isEqualTo(new BigDecimal("1.7470981"));
  }

  @Test
  void testCurrencyCalculations_noTrailingZeros() throws IOException {
    FixerClient fixerClient = mock(FixerClient.class);
    FtxClient   ftxClient   = mock(FtxClient.class);

    when(ftxClient.getSpotPrice(eq("FOO"), eq("USD"))).thenReturn(new BigDecimal("1.00000"));
    when(fixerClient.getConversionsForBase(eq("USD"))).thenReturn(Map.of(
        "EUR", new BigDecimal("0.200000"),
        "FJD", new BigDecimal("3.00000"),
        "FKP", new BigDecimal("50.0000"),
        "CAD", new BigDecimal("700.000")
    ));

    CurrencyConversionManager manager = new CurrencyConversionManager(fixerClient, ftxClient, List.of("FOO"));

    manager.updateCacheIfNecessary();

    CurrencyConversionEntityList conversions = manager.getCurrencyConversions().orElseThrow();

    assertThat(conversions.getCurrencies().size()).isEqualTo(1);
    assertThat(conversions.getCurrencies().get(0).getBase()).isEqualTo("FOO");
    assertThat(conversions.getCurrencies().get(0).getConversions().size()).isEqualTo(5);
    assertThat(conversions.getCurrencies().get(0).getConversions().get("USD")).isEqualTo(new BigDecimal("1"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("EUR")).isEqualTo(new BigDecimal("0.2"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("FJD")).isEqualTo(new BigDecimal("3"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("FKP")).isEqualTo(new BigDecimal("50"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("CAD")).isEqualTo(new BigDecimal("700"));
  }

  @Test
  void testCurrencyCalculations_accuracy() throws IOException {
    FixerClient fixerClient = mock(FixerClient.class);
    FtxClient   ftxClient   = mock(FtxClient.class);

    when(ftxClient.getSpotPrice(eq("FOO"), eq("USD"))).thenReturn(new BigDecimal("0.999999"));
    when(fixerClient.getConversionsForBase(eq("USD"))).thenReturn(Map.of(
        "EUR", new BigDecimal("1.000001"),
        "FJD", new BigDecimal("0.000001"),
        "FKP", new BigDecimal("1")
    ));

    CurrencyConversionManager manager = new CurrencyConversionManager(fixerClient, ftxClient, List.of("FOO"));

    manager.updateCacheIfNecessary();

    CurrencyConversionEntityList conversions = manager.getCurrencyConversions().orElseThrow();

    assertThat(conversions.getCurrencies().size()).isEqualTo(1);
    assertThat(conversions.getCurrencies().get(0).getBase()).isEqualTo("FOO");
    assertThat(conversions.getCurrencies().get(0).getConversions().size()).isEqualTo(4);
    assertThat(conversions.getCurrencies().get(0).getConversions().get("USD")).isEqualTo(new BigDecimal("0.999999"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("EUR")).isEqualTo(new BigDecimal("0.999999999999"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("FJD")).isEqualTo(new BigDecimal("0.000000999999"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("FKP")).isEqualTo(new BigDecimal("0.999999"));

  }

  @Test
  void testCurrencyCalculationsTimeoutNoRun() throws IOException {
    FixerClient fixerClient = mock(FixerClient.class);
    FtxClient   ftxClient   = mock(FtxClient.class);

    when(ftxClient.getSpotPrice(eq("FOO"), eq("USD"))).thenReturn(new BigDecimal("2.35"));
    when(fixerClient.getConversionsForBase(eq("USD"))).thenReturn(Map.of(
        "EUR", new BigDecimal("0.822876"),
        "FJD", new BigDecimal("2.0577"),
        "FKP", new BigDecimal("0.743446")
    ));

    CurrencyConversionManager manager = new CurrencyConversionManager(fixerClient, ftxClient, List.of("FOO"));

    manager.updateCacheIfNecessary();

    when(ftxClient.getSpotPrice(eq("FOO"), eq("USD"))).thenReturn(new BigDecimal("3.50"));

    manager.updateCacheIfNecessary();

    CurrencyConversionEntityList conversions = manager.getCurrencyConversions().orElseThrow();

    assertThat(conversions.getCurrencies().size()).isEqualTo(1);
    assertThat(conversions.getCurrencies().get(0).getBase()).isEqualTo("FOO");
    assertThat(conversions.getCurrencies().get(0).getConversions().size()).isEqualTo(4);
    assertThat(conversions.getCurrencies().get(0).getConversions().get("USD")).isEqualTo(new BigDecimal("2.35"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("EUR")).isEqualTo(new BigDecimal("1.9337586"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("FJD")).isEqualTo(new BigDecimal("4.835595"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("FKP")).isEqualTo(new BigDecimal("1.7470981"));
  }

  @Test
  void testCurrencyCalculationsFtxTimeoutWithRun() throws IOException {
    FixerClient fixerClient = mock(FixerClient.class);
    FtxClient   ftxClient   = mock(FtxClient.class);

    when(ftxClient.getSpotPrice(eq("FOO"), eq("USD"))).thenReturn(new BigDecimal("2.35"));
    when(fixerClient.getConversionsForBase(eq("USD"))).thenReturn(Map.of(
        "EUR", new BigDecimal("0.822876"),
        "FJD", new BigDecimal("2.0577"),
        "FKP", new BigDecimal("0.743446")
    ));

    CurrencyConversionManager manager = new CurrencyConversionManager(fixerClient, ftxClient, List.of("FOO"));

    manager.updateCacheIfNecessary();

    when(ftxClient.getSpotPrice(eq("FOO"), eq("USD"))).thenReturn(new BigDecimal("3.50"));
    manager.setFtxUpdatedTimestamp(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2) - TimeUnit.SECONDS.toMillis(1));
    manager.updateCacheIfNecessary();

    CurrencyConversionEntityList conversions = manager.getCurrencyConversions().orElseThrow();

    assertThat(conversions.getCurrencies().size()).isEqualTo(1);
    assertThat(conversions.getCurrencies().get(0).getBase()).isEqualTo("FOO");
    assertThat(conversions.getCurrencies().get(0).getConversions().size()).isEqualTo(4);
    assertThat(conversions.getCurrencies().get(0).getConversions().get("USD")).isEqualTo(new BigDecimal("3.5"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("EUR")).isEqualTo(new BigDecimal("2.880066"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("FJD")).isEqualTo(new BigDecimal("7.20195"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("FKP")).isEqualTo(new BigDecimal("2.602061"));
  }


  @Test
  void testCurrencyCalculationsFixerTimeoutWithRun() throws IOException {
    FixerClient fixerClient = mock(FixerClient.class);
    FtxClient   ftxClient   = mock(FtxClient.class);

    when(ftxClient.getSpotPrice(eq("FOO"), eq("USD"))).thenReturn(new BigDecimal("2.35"));
    when(fixerClient.getConversionsForBase(eq("USD"))).thenReturn(Map.of(
        "EUR", new BigDecimal("0.822876"),
        "FJD", new BigDecimal("2.0577"),
        "FKP", new BigDecimal("0.743446")
    ));

    CurrencyConversionManager manager = new CurrencyConversionManager(fixerClient, ftxClient, List.of("FOO"));

    manager.updateCacheIfNecessary();

    when(ftxClient.getSpotPrice(eq("FOO"), eq("USD"))).thenReturn(new BigDecimal("3.50"));
    when(fixerClient.getConversionsForBase(eq("USD"))).thenReturn(Map.of(
        "EUR", new BigDecimal("0.922876"),
        "FJD", new BigDecimal("2.0577"),
        "FKP", new BigDecimal("0.743446")
    ));

    manager.setFixerUpdatedTimestamp(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2) - TimeUnit.SECONDS.toMillis(1));
    manager.updateCacheIfNecessary();

    CurrencyConversionEntityList conversions = manager.getCurrencyConversions().orElseThrow();

    assertThat(conversions.getCurrencies().size()).isEqualTo(1);
    assertThat(conversions.getCurrencies().get(0).getBase()).isEqualTo("FOO");
    assertThat(conversions.getCurrencies().get(0).getConversions().size()).isEqualTo(4);
    assertThat(conversions.getCurrencies().get(0).getConversions().get("USD")).isEqualTo(new BigDecimal("2.35"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("EUR")).isEqualTo(new BigDecimal("2.1687586"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("FJD")).isEqualTo(new BigDecimal("4.835595"));
    assertThat(conversions.getCurrencies().get(0).getConversions().get("FKP")).isEqualTo(new BigDecimal("1.7470981"));
  }

}

//package com.shootingplace.shootingplace.services;
//
//import org.junit.runner.RunWith;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@RunWith(MockitoJUnitRunner.class)
//@SpringBootTest
//public class HistoryServiceTest {
//
//    @Mock
//    LicensePaymentHistoryRepository licensePaymentHistoryRepository;
//
//    @InjectMocks
//    HistoryService historyService;
//
//    List<LicensePaymentHistoryEntity> licenseHistoryList = createPaymentsList();

//    @Test
//    public void toggle_licence_payment_in_PZSS_return_bad_request_no_payment() {
//        //given
//        String uuid = String.valueOf(UUID.randomUUID());
//        //when
//        when(licensePaymentHistoryRepository.existsById(any(String.class))).thenReturn(existById(uuid));
//        ResponseEntity<?> responseEntity = historyService.toggleLicencePaymentInPZSS(uuid, true);
//        //then
//        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.BAD_REQUEST));
//        assertThat(responseEntity.getBody(), Matchers.equalTo("Nie znaleziono płatności"));
//    }
//
//    @Test
//    public void toggle_licence_payment_in_PZSS_return_OK() {
//        //given
//        String uuid = licenseHistoryList.get(0).getUuid();
//        //when
//        when(licensePaymentHistoryRepository.existsById(any(String.class))).thenReturn(existById(uuid));
//        when(licensePaymentHistoryRepository.findById(any(String.class))).thenReturn(Optional.ofNullable(findById(uuid)));
//        ResponseEntity<?> responseEntity = historyService.toggleLicencePaymentInPZSS(uuid,true,"0127");
//        //then
//        assertThat(responseEntity.getStatusCode(), Matchers.equalTo(HttpStatus.OK));
//        assertThat(responseEntity.getBody(), Matchers.equalTo("\"Oznaczono jako opłacone w Portalu PZSS\""));
//    }
//
//    private LicensePaymentHistoryEntity findById(String uuid) {
//        return licenseHistoryList.stream().filter(f->f.getUuid().equals(uuid)).findFirst().orElseThrow(EntityNotFoundException::new);
//    }
//
//    private Boolean existById(String uuid) {
//        return licenseHistoryList.stream().anyMatch(f->f.getUuid().equals(uuid));
//    }
//
//
//    public List<LicensePaymentHistoryEntity> createPaymentsList(){
//        List<LicensePaymentHistoryEntity> list = new ArrayList<>();
//        LicensePaymentHistoryEntity build = LicensePaymentHistoryEntity.builder()
//                .uuid(String.valueOf(UUID.randomUUID()))
//                .isPayInPZSSPortal(false)
//                .validForYear(2021)
//                .date(LocalDate.now())
//                .memberUUID(String.valueOf(UUID.randomUUID()))
//                .build();
//        list.add(build);
//        return list;
//    }
//}
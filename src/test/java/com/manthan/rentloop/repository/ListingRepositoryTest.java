package com.manthan.rentloop.repository;

import com.manthan.rentloop.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ListingRepositoryTest {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Category category;
    private Listing listing;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@listingrepo.com");
        owner.setPasswordHash("hash");
        owner.setRole(Role.USER);
        owner = userRepository.save(owner);

        category = new Category();
        category.setName("Cameras");
        category.setDescription("DSLR and Mirrorless");
        category = categoryRepository.save(category);

        Item item = new Item();
        item.setOwner(owner);
        item.setCategory(category);
        item.setName("Sony Camera");
        item.setCondition(ItemCondition.GOOD);
        item = itemRepository.save(item);

        listing = new Listing();
        listing.setItem(item);
        listing.setTitle("Sony Alpha Camera");
        listing.setPricePerDay(BigDecimal.valueOf(40.00));
        listing.setDepositAmount(BigDecimal.valueOf(100.00));
        listing.setLatitude(BigDecimal.valueOf(37.7749));
        listing.setLongitude(BigDecimal.valueOf(-122.4194));
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);
    }

    @Test
    void findByIdWithLock_ExecutesPessimisticWriteLockQuery() {
        Optional<Listing> found = listingRepository.findByIdWithLock(listing.getId());

        assertTrue(found.isPresent());
        assertEquals("Sony Alpha Camera", found.get().getTitle());
    }

    @Test
    void searchListings_FiltersByCategoryAndPrice() {
        Page<Listing> page = listingRepository.searchListings(
                category.getId(),
                BigDecimal.valueOf(30.00),
                BigDecimal.valueOf(50.00),
                null,
                null,
                PageRequest.of(0, 10)
        );

        assertEquals(1, page.getContent().size());
        assertEquals(listing.getId(), page.getContent().get(0).getId());
    }

    @Test
    void findMatchingListings_MatchesCategoryAndBudget() {
        List<Listing> matches = listingRepository.findMatchingListings(
                category.getId(),
                BigDecimal.valueOf(50.00),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5)
        );

        assertEquals(1, matches.size());
        assertEquals(listing.getId(), matches.get(0).getId());
    }
}

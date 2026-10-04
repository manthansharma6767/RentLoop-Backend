package com.manthan.rentloop.repository;

import com.manthan.rentloop.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ListingRepository listingRepository;

    private User owner;
    private User renter;
    private Listing listing;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@repo.com");
        owner.setPasswordHash("hash");
        owner.setRole(Role.USER);
        owner = userRepository.save(owner);

        renter = new User();
        renter.setName("Renter");
        renter.setEmail("renter@repo.com");
        renter.setPasswordHash("hash");
        renter.setRole(Role.USER);
        renter = userRepository.save(renter);

        Category category = new Category();
        category.setName("Electronics");
        category.setDescription("Devices");
        category = categoryRepository.save(category);

        Item item = new Item();
        item.setOwner(owner);
        item.setCategory(category);
        item.setName("Laptop");
        item.setCondition(ItemCondition.GOOD);
        item = itemRepository.save(item);

        listing = new Listing();
        listing.setItem(item);
        listing.setTitle("Gaming Laptop");
        listing.setPricePerDay(BigDecimal.valueOf(25.00));
        listing.setDepositAmount(BigDecimal.valueOf(50.00));
        listing.setLatitude(BigDecimal.valueOf(37.7749));
        listing.setLongitude(BigDecimal.valueOf(-122.4194));
        listing.setStatus(ListingStatus.ACTIVE);
        listing = listingRepository.save(listing);

        Booking booking = new Booking();
        booking.setListing(listing);
        booking.setRenter(renter);
        booking.setStartDate(LocalDate.of(2026, 10, 10));
        booking.setEndDate(LocalDate.of(2026, 10, 15));
        booking.setTotalAmount(BigDecimal.valueOf(125.00));
        booking.setDepositAmount(BigDecimal.valueOf(50.00));
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    @Test
    void existsOverlappingBooking_ConflictingDates_ReturnsTrue() {
        boolean overlap = bookingRepository.existsOverlappingBooking(
                listing.getId(),
                List.of(BookingStatus.CONFIRMED, BookingStatus.ACTIVE),
                LocalDate.of(2026, 10, 12),
                LocalDate.of(2026, 10, 18)
        );

        assertTrue(overlap);
    }

    @Test
    void existsOverlappingBooking_NonConflictingDates_ReturnsFalse() {
        boolean overlap = bookingRepository.existsOverlappingBooking(
                listing.getId(),
                List.of(BookingStatus.CONFIRMED, BookingStatus.ACTIVE),
                LocalDate.of(2026, 10, 16),
                LocalDate.of(2026, 10, 20)
        );

        assertFalse(overlap);
    }

    @Test
    void findByRenterEmail_ReturnsBookings() {
        List<Booking> bookings = bookingRepository.findByRenterEmail("renter@repo.com");

        assertEquals(1, bookings.size());
        assertEquals("renter@repo.com", bookings.get(0).getRenter().getEmail());
    }

    @Test
    void findByListingItemOwnerEmail_ReturnsBookings() {
        List<Booking> bookings = bookingRepository.findByListingItemOwnerEmail("owner@repo.com");

        assertEquals(1, bookings.size());
        assertEquals("owner@repo.com", bookings.get(0).getListing().getItem().getOwner().getEmail());
    }
}

package com.manthan.rentloop.service;

import com.manthan.rentloop.dto.BookingResponse;
import com.manthan.rentloop.dto.ListingResponse;
import com.manthan.rentloop.dto.RentalRequestResponse;
import com.manthan.rentloop.dto.ReviewDto;
import com.manthan.rentloop.dto.UserProfileDto;
import com.manthan.rentloop.model.*;
import com.manthan.rentloop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ListingRepository listingRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private RentalRequestRepository rentalRequestRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ItemService itemService;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private User owner;
    private Category category;
    private Item item;
    private Listing listing;
    private Booking booking;
    private RentalRequest rentalRequest;
    private Review review;

    @BeforeEach
    void setUp() {
        // User
        user = new User();
        user.setId(1L);
        user.setName("Alice");
        user.setEmail("alice@example.com");
        user.setRole(Role.USER);
        user.setCreatedAt(Instant.now());

        // Owner
        owner = new User();
        owner.setId(2L);
        owner.setName("Bob");
        owner.setEmail("bob@example.com");
        owner.setRole(Role.USER);
        owner.setCreatedAt(Instant.now());

        // Category
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        // Item
        item = new Item();
        item.setId(1L);
        item.setOwner(owner);
        item.setCategory(category);
        item.setName("Camera");
        item.setCondition(ItemCondition.GOOD);

        // Listing
        listing = new Listing();
        listing.setId(10L);
        listing.setItem(item);
        listing.setTitle("Camera Rental");
        listing.setPricePerDay(new BigDecimal("50.00"));
        listing.setDepositAmount(new BigDecimal("200.00"));
        listing.setLatitude(new BigDecimal("12.971600"));
        listing.setLongitude(new BigDecimal("77.594600"));
        listing.setStatus(ListingStatus.ACTIVE);

        // Booking
        booking = new Booking();
        booking.setId(20L);
        booking.setListing(listing);
        booking.setRenter(user);
        booking.setStartDate(LocalDate.now());
        booking.setEndDate(LocalDate.now().plusDays(3));
        booking.setTotalAmount(new BigDecimal("150.00"));
        booking.setDepositAmount(new BigDecimal("200.00"));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(Instant.now());

        // RentalRequest
        rentalRequest = new RentalRequest();
        rentalRequest.setId(30L);
        rentalRequest.setRequester(user);
        rentalRequest.setCategory(category);
        rentalRequest.setDescription("Need a camera for a week");
        rentalRequest.setBudgetPerDay(new BigDecimal("60.00"));
        rentalRequest.setStartDate(LocalDate.now());
        rentalRequest.setEndDate(LocalDate.now().plusDays(7));
        rentalRequest.setLatitude(new BigDecimal("12.971600"));
        rentalRequest.setLongitude(new BigDecimal("77.594600"));
        rentalRequest.setStatus(RentalRequestStatus.PENDING);
        rentalRequest.setCreatedAt(Instant.now());

        // Review
        review = new Review();
        review.setId(40L);
        review.setBookingId(20L);
        review.setReviewerEmail("alice@example.com");
        review.setTargetEmail("bob@example.com");
        review.setListingId(10L);
        review.setRating(4);
        review.setComment("Good rental");
        review.setCreatedAt(Instant.now());
    }

    // ─── USER MANAGEMENT ─────────────────────────────────────────────────────

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(user, owner));

        List<UserProfileDto> result = adminService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("alice@example.com", result.get(0).getEmail());
        assertEquals("USER", result.get(0).getRole());
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileDto result = adminService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Alice", result.getName());
        assertEquals("alice@example.com", result.getEmail());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminService.getUserById(999L));

        assertTrue(ex.getMessage().contains("User not found with ID: 999"));
    }

    // ─── LISTING MODERATION ──────────────────────────────────────────────────

    @Test
    void getAllListings_Success() {
        when(listingRepository.findAll()).thenReturn(List.of(listing));
        when(itemService.mapToResponse(item)).thenReturn(new com.manthan.rentloop.dto.ItemResponse());

        List<ListingResponse> result = adminService.getAllListings();

        assertEquals(1, result.size());
        assertEquals("Camera Rental", result.get(0).getTitle());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }

    @Test
    void adminRemoveListing_Success() {
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));

        adminService.adminRemoveListing(10L);

        assertEquals(ListingStatus.REMOVED, listing.getStatus());
        verify(listingRepository, times(1)).save(listing);
    }

    @Test
    void adminRemoveListing_NotFound_ThrowsException() {
        when(listingRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminService.adminRemoveListing(999L));

        assertTrue(ex.getMessage().contains("Listing not found with ID: 999"));
        verify(listingRepository, never()).save(any());
    }

    // ─── BOOKING OVERSIGHT ───────────────────────────────────────────────────

    @Test
    void getAllBookings_Success() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        List<BookingResponse> result = adminService.getAllBookings();

        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getId());
        assertEquals("CONFIRMED", result.get(0).getStatus());
        assertEquals("Camera Rental", result.get(0).getListingTitle());
        assertEquals("Alice", result.get(0).getRenterName());
        assertEquals("Bob", result.get(0).getOwnerName());
    }

    // ─── RENTAL REQUEST OVERSIGHT ────────────────────────────────────────────

    @Test
    void getAllRentalRequests_Success() {
        when(rentalRequestRepository.findAll()).thenReturn(List.of(rentalRequest));

        List<RentalRequestResponse> result = adminService.getAllRentalRequests();

        assertEquals(1, result.size());
        assertEquals(30L, result.get(0).getId());
        assertEquals("PENDING", result.get(0).getStatus());
        assertEquals("Alice", result.get(0).getRequesterName());
        assertEquals("Electronics", result.get(0).getCategoryName());
    }

    // ─── REVIEW MODERATION ───────────────────────────────────────────────────

    @Test
    void getAllReviews_Success() {
        when(reviewRepository.findAll()).thenReturn(List.of(review));

        List<ReviewDto> result = adminService.getAllReviews();

        assertEquals(1, result.size());
        assertEquals(40L, result.get(0).getId());
        assertEquals(4, result.get(0).getRating());
        assertEquals("alice@example.com", result.get(0).getReviewerEmail());
    }

    @Test
    void adminDeleteReview_Success() {
        when(reviewRepository.existsById(40L)).thenReturn(true);

        adminService.adminDeleteReview(40L);

        verify(reviewRepository, times(1)).deleteById(40L);
    }

    @Test
    void adminDeleteReview_NotFound_ThrowsException() {
        when(reviewRepository.existsById(999L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminService.adminDeleteReview(999L));

        assertTrue(ex.getMessage().contains("Review not found with ID: 999"));
        verify(reviewRepository, never()).deleteById(any());
    }
}

# 코드 스타일 컨벤션

### 코드 스타일(intellij)

---

[](https://blog.naver.com/seek316/223306071687)

- Google Code Style 적용
- Tab size : 4
- Indent: 4
- Continuation indent: 4

!image.png

# Java 어플리케이션 코드 컨벤션

---

## <클래스와 메서드 명명 규칙>

---

- 클래스: PascalCase (예시 UserController, OrderService)
- 메서드 및 변수: camelCase (예시 findUserById, isOrderValid)
- 상수: ALL_CAPS (예시 MAX_RETRY_ATTEMPTS)

<aside>
💡

**메서드나 변수명은 길어져도 상관없으니까 최대한 자세하게 알아볼 수 있게 작성**

</aside>

- 참고자료

메서드,클래스 명명규칙

# Layer 별 코드 컨벤션

---

## Controller

```java
	@GetMapping("/{userId}")
	public ResponseEntity<BaseResponse<UserInfoResponseDto>> getUserInfo(
		@PathVariable Long userId
){
		UserInfoResponseDto userInfoResponseDto = userService.getUserInfo(userId);
		
		return ResponseEntity.ok(
			BaseResponse.success("유저 조회에 성공했습니다.", userInfoResponseDto));
	}
```

### 1. 성공 응답 (Success)

- **성공-기본 (`HttpStatus`, `message`, `data`)**
    - `201 Created` 등 200 외의 특정 성공 상태 코드를 직접 지정하여 데이터와 함께 반환할 때 사용
- **성공-ok (`message`, `data`)**
    - 가장 일반적인 성공 케이스로, 상태 코드 지정 없이 기본값 `200 OK`와 데이터를 함께 반환할 때 사용
- **성공-no_content (`message`)**
    - 수정·삭제 등 보낼 데이터는 없으나, 프론트엔드에 `204 No Content` 상태와 완료 메시지를 바디에 담아 전달할 때 사용

### 2. 실패 응답 (Fail)

- **실패-기본 (`ErrorCode`)**
    - 정의된 `ErrorCode` Enum을 바탕으로 고정된 상태 코드, 에러 코드명(`code`), 에러 메시지를 자동 매핑하여 반환할 때 사용
- **실패-메시지 커스텀 (`ErrorCode`, `message`)**
    - 동일한 에러 코드(예: `BAD_REQUEST`)를 쓰지만, 유효성 검증 실패 등 구체적인 에러 메시지만 동적으로 변경하여 반환할 때 사용
- **실패-상태 코드 및 메시지 커스텀 (`HttpStatus`, `message`)**
    - Enum에 정의되지 않은 특수 예외이거나, 세부 에러 코드명 없이 가벼운 HTTP 상태와 메시지만으로 실패를 반환할 때 사용

## Entity

```java
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = {"email"}),
                @UniqueConstraint(name = "uk_user_nickname", columnNames = {"nickname"})
        }
)
public class User extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String email;
    
	  @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private *UserRole* userRole;
    ...
}
```

## DTO

### xxxRequestDto

```java
public record UserRequestDto(
        String email,
        String nickname,
        String profileImage,
				String phoneNumber,
				...
) { }
```

### xxxResponseDto

```java
public record UserResponseDto(
        Long userId,
        String email,
        String nickname,
        String profileImage,
				String phoneNumber,
        String description,
        Long balance,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static UserInfoResponseDto from(User user) {
        return new UserInfoResponseDto(
                user.getId(),
                user.getEmail(),
                ...
        );
    }
}
```

from

- Dto계층에서 Entity를 변환할 때 메서드명은 from 사용

of

- Dto계층에서 Entity의 일부 값을 꺼낼 때 사용
    - PageInfo, SortInfo 외에는 잘 없을 겁니다.

### ListResponseDto - 공통 양식 사용 예시

```java
// Controller
@GetMapping
public ResponseEntity<BaseResponse<ListResponseDto<CategoryResponseDto>>> getCategories() {
    
    List<CategoryResponseDto> categories = categoryService.findAll();

    return ResponseEntity.ok(
        BaseResponse.success("카테고리 조회 성공", ListResponseDto.from(categories))
    );
}

// ----------------------------

// Service
public List<CategoryResponseDto> findAll() {
    return categoryRepository.findAll().stream()
        .map(CategoryResponseDto::from)
        .toList();
}
```

### PageResponseDto - 공통 양식 사용 예시

```java
// Controller
@GetMapping
public ResponseEntity<BaseResponse<PageResponseDto<AuctionResponseDto>>> getAuctions(
	    @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
) {
    Page<AuctionResponseDto> auctions = auctionService.findAll(auctions(pageable);

    return ResponseEntity.ok(
        BaseResponse.success("경매 조회 성공", PageResponseDto.from(auctions))
    );
}

// ---------------------------

// Service
@Override
public Page<AuctionResponseDto> findAll(Pageable pageable) {
        return auctionRepository.findAll(pageable)
            .map(AuctionResponseDto::from);
}
```

---

## Service

```java
	// 서비스 메서드 이름에 대해 컨벤션 잡으면 깔끔해집니다.
	// 이 외에 특정 작업을 수행하는 경우, 자유롭게 작성
	
	// select -> get...
	// insert -> save...
	// update -> update...
	// delete -> delete...
```

---

## Repository

```java
public interface UserRepository extends JpaRepository<User, Long> {

}
```

### QueryDSL 사용 시

### xxxQueryRepository
\\ xxxQueryRepositoryImpl

- QueryDSL용 interface 생성후, 기존 사용하시던 JpaRepository를 extends한 인터페이스에 추가로 extends 시키시면 됩니다.
- 실제 코드 작성은 QueryDSL용 interface를 implments한 클래스에 진행해주시면 됩니다.

```java
public interface UserRepository extends JpaRepository<User, Long>,
    UserQueryRepository {

}
```

!image.png

---

api에 서비스 로직에 log를 다 적기
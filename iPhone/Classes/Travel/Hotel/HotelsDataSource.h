//
//  HotelsNearMeDataSource.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 6/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AbstractDataSource.h"
#import "ConcreteDataSourceSectionInfo.h"
#import "CTELocation.h"
#import "CTEError.h"

@interface HotelsDataSource : AbstractDataSource

@property (nonatomic, strong) ConcreteDataSourceSectionInfo *hotelListSection;
@property (copy,nonatomic) void(^onSearchError)(NSString *error);
@property (copy,nonatomic) void(^hideWaitView)();
@property (copy,nonatomic) void(^afterDoneSearch)();
@property BOOL searchDone;

- (BOOL) isSearchCriteriaValid;
- (BOOL) isSpecificCity;                   // Whether user choose city other than current location

/*! Toggles the Search cirteria section
 */
- (void) toggleSearchCriteriaSection;
- (void) updateDestination: (CTELocation *)cteLocation;
- (void) updateHotelNameCriteria:(NSString *)hotelName;
- (void) insertDatePicker:(NSIndexPath *)index;
- (void) removeDatePicker:(NSIndexPath *)index;
- (void) insertSearchDistancePicker:(NSIndexPath *)index;
- (void) updateHotelSearchCriteria;
- (void) searchHotels;
- (void) searchHotelsNearMe;

- (void) updateDate:(NSDate *)targetDate atIndexPath:indexPath;
- (NSString *)getHotelNameCriteria;

/*! Sorting functions which modify the searchResultList array
 */
- (void) sortSearchResultListByDistance;
- (void) sortSearchResultListByPreferred;
- (void) sortSearchResultListByStarRating;
- (void) sortSearchResultListByHotelRate;
- (void) sortSearchResultListByRecommendationScore;

/*! Filter functions which modify the searchResultList array
    But has a search list copy property for store.
 */
- (void) filterSearchResultList;
- (void) updateFilterDict:(NSMutableDictionary *)dict;
@end

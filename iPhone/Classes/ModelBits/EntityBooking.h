//
//  EntityBooking.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/11/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntitySegment, EntityTrip;

@interface EntityBooking : NSManagedObject

@property (nonatomic, strong) NSString * recordLocator;
@property (nonatomic, strong) NSString * travelConfigId;
@property (nonatomic, strong) NSString * agencyPCC;
@property (nonatomic, strong) NSString * companyAccountingCode;
@property (nonatomic, strong) NSString * dateBookedLocal;
@property (nonatomic, strong) NSString * type;
@property (nonatomic, strong) NSString * label;
@property (nonatomic, strong) NSString * gds;
@property (nonatomic, strong) NSString * bookSource;
@property (nonatomic, strong) NSNumber * isCliqbookSystemOfRecord;
@property (nonatomic, strong) EntityTrip *relTrip;
@property (nonatomic, strong) NSSet *relSegment;
@end

@interface EntityBooking (CoreDataGeneratedAccessors)

- (void)addRelSegmentObject:(EntitySegment *)value;
- (void)removeRelSegmentObject:(EntitySegment *)value;
- (void)addRelSegment:(NSSet *)values;
- (void)removeRelSegment:(NSSet *)values;
@end

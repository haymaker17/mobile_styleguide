//
//  CTETravelRequestDigestCellInfos.h
//  ConcurSDK
//
//  Created by laurent mery on 03/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
@class CTETravelRequestEntry;

@interface CTETravelRequestDigestCellInfos : NSObject

@property (strong, nonatomic) NSString *Icon;
@property (strong, nonatomic) NSString *SegmentName;
@property (strong, nonatomic) NSString *Amount;
@property (strong, nonatomic) NSString *CurrencyCode;
@property (strong, nonatomic) NSString *EarlyDate;
@property (strong, nonatomic) NSString *Location1;
@property (strong, nonatomic) NSString *Location2;

-(id)initWithEntry:(CTETravelRequestEntry *)entry;

@end

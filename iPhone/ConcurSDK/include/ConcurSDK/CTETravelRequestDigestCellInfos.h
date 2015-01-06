//
//  CTETravelRequestDigestCellInfos.h
//  ConcurSDK
//
//  Created by laurent mery on 03/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEDataTypes.h"
@class CTETravelRequestEntry;

@interface CTETravelRequestDigestCellInfos : NSObject

@property (copy, nonatomic) CTEDataTypes *Icon;
@property (copy, nonatomic) CTEDataTypes *SegmentName;
@property (copy, nonatomic) CTEDataTypes *Amount;
@property (copy, nonatomic) CTEDataTypes *CurrencyCode;
@property (copy, nonatomic) CTEDataTypes *EarlyDate;
@property (copy, nonatomic) CTEDataTypes *Location1;
@property (copy, nonatomic) CTEDataTypes *Location2;

-(id)initWithEntry:(CTETravelRequestEntry *)entry;

@end

//
//  SegmentRow.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/22/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SegmentRow.h"


@implementation SegmentRow

@synthesize			rowLabel, rowValue, url, iconName, imageName, mapAddress, specialCellType, viewTitle, mapNote, mapAnnotation, vendorType;
@synthesize			section, row, valueFontSize;
@synthesize			showDisclosure, isSpecialCell, isMap, isPhone, isWeb, isVendorRow, isAirVendor, isAirport, isSeat;
@synthesize			longi, lati;
@synthesize			segment;
@synthesize			vendorImage, isEmblazoned, iataCode, isApp, isSep, color, isCancel, isFlightStats, isOperatedBy, isDescription, isFlightSchedule;
@synthesize         isCopyEnable;

@synthesize railSegOniPad;

-(id)init
{
    self = [super init];
    if (self) {
        self.railSegOniPad = YES;
    }
    return self;
}
@end

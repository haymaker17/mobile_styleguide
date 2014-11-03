//
//  SegmentRow.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/22/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SegmentData.h"
#import "EntitySegment.h"

@interface SegmentRow : NSObject
{
	NSString			*rowLabel, *rowValue, *url, *iconName, *imageName, *mapAddress, *specialCellType, *viewTitle, *mapNote, *mapAnnotation, *vendorType, *iataCode;
	int					section, row, valueFontSize;
	BOOL				showDisclosure, isSpecialCell, isMap, isPhone, isWeb, isVendorRow, isEmblazoned, isAirVendor, isAirport, isSeat, isApp, isSep, isCancel, isFlightStats, isOperatedBy, isDescription, isFlightSchedule;
	float				longi, lati;
	EntitySegment		*segment;
	UIImage				*vendorImage;
	UIColor				*color;
    BOOL                railSegOniPad;
}

@property BOOL isDescription;
@property (strong, nonatomic) NSString			*rowLabel;
@property (strong, nonatomic) NSString			*rowValue;
@property (strong, nonatomic) NSString			*url;
@property (strong, nonatomic) NSString			*iconName;
@property (strong, nonatomic) NSString			*imageName;
@property (strong, nonatomic) NSString			*mapAddress;
@property (strong, nonatomic) NSString			*specialCellType;
@property (strong, nonatomic) NSString			*viewTitle;
@property (strong, nonatomic) NSString			*mapNote;
@property (strong, nonatomic) NSString			*mapAnnotation;
@property (strong, nonatomic) NSString			*vendorType;
@property (strong, nonatomic) NSString			*iataCode;

@property (strong, nonatomic) UIColor				*color;

@property int				section;
@property int				row;
@property int				valueFontSize;

@property BOOL				showDisclosure;
@property BOOL				isSpecialCell;
@property BOOL				isMap;
@property BOOL				isPhone;
@property BOOL				isWeb;
@property BOOL				isVendorRow;
@property BOOL				isEmblazoned;
@property BOOL				isAirVendor;
@property BOOL				isAirport;
@property BOOL				isSeat;
@property BOOL				isApp;
@property BOOL				isSep;
@property BOOL				isCancel;
@property BOOL				isOperatedBy;
@property BOOL              isCopyEnable;

@property float				longi;
@property float				lati;
@property BOOL				isFlightStats;
@property BOOL				isFlightSchedule;

@property(strong, nonatomic) EntitySegment		*segment;
@property (strong, nonatomic) UIImage			*vendorImage;
// Set this to false in SegmentStuff.m, if you want to diable one segment row for rail detail on iPad.
@property BOOL              railSegOniPad;
@end

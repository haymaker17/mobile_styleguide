//
//  DownloadSystemConfig.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"

@class SystemConfig;
@class ViolationReason;
@class OfficeLocationResult;

@interface DownloadSystemConfig : MsgResponder
{
	NSString				*currentElement;
	NSString				*path;
	NSMutableString			*buildString;
	SystemConfig			*systemConfig;
	BOOL					parsingHotelReasons;
	BOOL					parsingCarReasons;
    BOOL					parsingAirReasons;
	ViolationReason			*currentReason;
	OfficeLocationResult	*currentOffice;
}

@property BOOL checkboxDefault;
@property BOOL showCheckbox;
@property BOOL isRefundableInfo;
@property (nonatomic, strong) NSString              *nonRefundableMsg;
@property (nonatomic, strong) NSString				*currentElement;
@property (nonatomic, strong) NSString				*path;
@property (nonatomic, strong) NSMutableString		*buildString;
@property (nonatomic, strong) SystemConfig			*systemConfig;
@property (nonatomic) BOOL							parsingHotelReasons;
@property (nonatomic) BOOL							parsingCarReasons;
@property (nonatomic) BOOL							parsingAirReasons;
@property (nonatomic, strong) ViolationReason		*currentReason;
@property (nonatomic, strong) OfficeLocationResult	*currentOffice;


-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;

@end

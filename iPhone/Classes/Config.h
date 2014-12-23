//
//  Config.h
//  ConcurMobile
//
//  Created by AJ Cram on 4/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Config : NSObject

+(BOOL) isDevBuild;
+(BOOL) isDevConBuild;
+(BOOL) isSprintDemoBuild;
+(BOOL) isNetworkDebugEnabled;
+(BOOL) isSalesforceChatterEnabled;
+(BOOL) isGov;
+(BOOL) isCorpHome;
+(BOOL) isEvaVoiceEnabled;
+(BOOL) isNewEditingEnabled;
+(BOOL) isEnterprise;

// MOB-18709 - for new Travel session
+(BOOL) isNewTravel;
+(BOOL) isNewAirBooking;
+(BOOL) isNewHotelBooking;

+(BOOL) isNewSignInFlowEnabled;
+(BOOL) isNewVoiceUIEnabled;
+(BOOL) isTravelRequestEnabled;
+(BOOL) isEreceiptsEnabled;
+(BOOL) isTouchIDEnabled;
+(BOOL) isOCRExpenseEnabled;
+(BOOL) isProfileEnable;

@end

//
//  CarResult.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface CarResult : NSObject
{
}

@property (nonatomic, strong)	NSString*		carId;
@property (nonatomic, strong)   NSString*       choiceId;
@property (nonatomic, strong)	NSString*		currencyCode;
@property (nonatomic) double					dailyRate;
@property (nonatomic) double					totalRate;
@property (nonatomic) bool                      sendCreditCard;
@property (nonatomic, strong)	NSString*		freeMiles;
@property (nonatomic, strong)	NSString*		carType;
@property (nonatomic, strong)	NSString*		chainCode;
@property (nonatomic, strong)	NSString*		imageUri;
@property (nonatomic, strong)   NSNumber*       maxEnforcementLevel;
@property (nonatomic, strong)	NSMutableArray	*violations;
@property (nonatomic, strong)   NSString*       gdsName;

@end

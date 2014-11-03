//
//  CreditCardManager.h
//  ConcurMobile
//
//  Created by Richard Puckett on 11/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "RXMLElement.h"
#import "SimpleCreditCard.h"

@interface CreditCardManager : NSObject

@property (assign) NSUInteger selectedCardIndex;
@property (strong, nonatomic) NSMutableArray *cardList;

+ (CreditCardManager *)sharedInstance;
- (NSString *)cardAtIndex:(NSUInteger)index;
- (NSArray *)cards;
- (void)loadFromXml:(RXMLElement *)xml;
- (NSString *)selectedCard;
- (SimpleCreditCard *)selectedSimpleCard;

@end

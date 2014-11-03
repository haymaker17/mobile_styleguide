//
//  CreditCardManager.m
//  ConcurMobile
//
//  Created by Richard Puckett on 11/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CreditCardManager.h"
#import "SimpleCreditCard.h"

@implementation CreditCardManager

__strong static id _sharedInstance = nil;

+ (CreditCardManager *)sharedInstance {
    static dispatch_once_t once;
    
    dispatch_once(&once, ^{
        _sharedInstance = [[self alloc] init];
    });
    
    return _sharedInstance;
}

- (id)init {
    self = [super init];
    
    if (self) {
        self.cardList = [[NSMutableArray alloc] init];
        
        self.selectedCardIndex = 0;
    }
    
    return self;
}

- (NSArray *)cards {
    return self.cardList;
}

/* 
<MWSResponse xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
 <Response>
 <CreditCards>
 <CreditCardInfos>
 <CreditCardInfo>
 <CreditCardId>516</CreditCardId>
 <CreditCardLastFour>1111</CreditCardLastFour>
 <IsDefault>true</IsDefault>
 <Name>e.Card</Name>
 </CreditCardInfo>
 </CreditCardInfos>
 </CreditCards>
 </Response>
</MWSResponse>
 */
- (void)loadFromXml:(RXMLElement *)xml {
    [self.cardList removeAllObjects];
    
    [xml iterate:@"Response.CreditCards.CreditCardInfos.CreditCardInfo" usingBlock:^(RXMLElement *creditCardInfo) {
        SimpleCreditCard *card = [[SimpleCreditCard alloc] init];
        
        card.cardId = [creditCardInfo child:@"CreditCardId"].text;
        card.cardName = [creditCardInfo child:@"Name"].text;
        card.isDefault = [[creditCardInfo child:@"IsDefault"].text boolValue];
        card.lastFourDigits = [creditCardInfo child:@"CreditCardLastFour"].text;
        
        [self.cardList addObject:card];
    }];
}

- (NSString *)selectedCard {
    NSString *c = @"";
    
    if ([self.cardList count] > 0) {
        SimpleCreditCard *card = [self.cardList objectAtIndex:self.selectedCardIndex];
    
        c = [NSString stringWithFormat:@"%@ (%@)", card.cardName, card.lastFourDigits];
    }
    
    return c;
}

- (SimpleCreditCard *)selectedSimpleCard {
    SimpleCreditCard *c = nil;
    
    if ([self.cardList count] > 0) {
        c = [self.cardList objectAtIndex:self.selectedCardIndex];
    }
    
    return c;
}

- (NSString *)cardAtIndex:(NSUInteger)index {
    NSString *c = @"";
    
    if ([self.cardList count] > index) {
        SimpleCreditCard *card = [self.cardList objectAtIndex:index];
        
        c = [NSString stringWithFormat:@"%@ (%@)", card.cardName, card.lastFourDigits];
    }
    
    return c;
}

@end

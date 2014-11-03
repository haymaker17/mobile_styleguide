//
//  GovPerDiemRate.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovPerDiemRate : NSObject
{
    NSString            *perDiemId; // TabRow
    NSString            *location;
    NSString            *locState;
    NSDate              *effectiveDate;
    NSDate              *expirationDate;
    NSDecimalNumber     *ldgRate;
    NSDecimalNumber     *mieRate;
    NSString            *crnCode;
}

@property (nonatomic, strong) NSString              *location;
@property (nonatomic, strong) NSString              *locState;
@property (nonatomic, strong) NSString              *perDiemId;
@property (nonatomic, strong) NSDate                *effectiveDate;
@property (nonatomic, strong) NSDate                *expirationDate;
@property (nonatomic, strong) NSDecimalNumber       *ldgRate;
@property (nonatomic, strong) NSDecimalNumber       *mieRate;
@property (nonatomic, strong) NSString              *crnCode;

/*<locate>RICHLAND</locate>
<locst>WA</locst>
<effdate>2012-10-01</effdate>
<snl-start>1992-01-01</snl-start>
<snl-end>1992-12-31</snl-end>
<ldgrate>93.00</ldgrate>
<mierate>46.00</mierate>
<expdate>2049-12-31</expdate>
<snl-name/>
<comment/>
<ftnote-rate>0.00</ftnote-rate>
<incid-amt>5.00</incid-amt>
<custom-rt-org/>
<currency>USD</currency>
<extra-char1/>
<extra-char2/>
<extra-dec1>0.00</extra-dec1>
<extra-dec2>0.00</extra-dec2>
<extra-date1 xsi:nil="true"/>
<TabRow>0x00000000000dcac7</TabRow>*/
@end

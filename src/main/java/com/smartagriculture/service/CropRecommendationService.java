package com.smartagriculture.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CropRecommendationService {

    /**
     * Generates tailored agricultural protection actions
     */
    public List<String> getRecommendations(String cropType, String riskLevel, String lang) {
        String crop = cropType != null ? cropType.trim().toLowerCase() : "general";
        String risk = riskLevel != null ? riskLevel.trim().toUpperCase() : "MEDIUM";
        String language = lang != null ? lang.trim().toLowerCase() : "en";

        List<String> list = new ArrayList<>();

        if ("te".equals(language)) {
            list.addAll(getTeluguRecommendations(crop, risk));
        } else if ("hi".equals(language)) {
            list.addAll(getHindiRecommendations(crop, risk));
        } else {
            list.addAll(getEnglishRecommendations(crop, risk));
        }

        return list;
    }

    private List<String> getEnglishRecommendations(String crop, String risk) {
        List<String> r = new ArrayList<>();
        if ("HIGH".equals(risk)) {
            switch (crop) {
                case "rice":
                case "paddy":
                    r.add("POSTPONE HARVEST: Strong rain warning. Standing paddy is safer than harvested heaps exposed to rainwater.");
                    r.add("DRAINAGE: Open field channels to drain stagnant standing water immediately.");
                    r.add("SHELTER CUT CROPS: If already harvested, move sheaves immediately onto elevated racks or cover with waterproof tarpaulins.");
                    r.add("STORAGE: Ensure harvested paddy has moisture below 14% before bagged storage to prevent fungal discolouration.");
                    break;
                case "wheat":
                    r.add("SUSPEND HARVESTING: Suspend combine harvesting until the rain spell passes and the moisture drops.");
                    r.add("PREVENT LODGING: Avoid irrigation and secure border drainage to stop root softening.");
                    r.add("COVER THRESHED GRAIN: Ensure threshed grain piles in mandis or yards are covered with polythene sheets.");
                    r.add("BLACK POINT WARNING: Excess dampness causes grain darkening; dry harvested lots under covered sheds.");
                    break;
                case "cotton":
                    r.add("PICK OPEN BOLLS IMMEDIATELY: Harvest opened, mature fluffy cotton bolls prior to rainfall to avoid lint staining.");
                    r.add("DO NOT PICK WET BOLLS: Post-rain, do not harvest wet bolls as fiber quality downgrades and rots.");
                    r.add("INDOOR STORAGE: Move picked cotton into well-ventilated dry rooms; avoid stacking on bare soil.");
                    r.add("BOLL ROT ALERT: Ensure field ridges drain water freely to prevent bottom boll rotting.");
                    break;
                case "maize":
                case "corn":
                    r.add("DELAY COB HARVEST: Do not detach cobs in rain unless mature and immediate drying fans/cribs are ready.");
                    r.add("COVER STORED COBS: Rainwater ingress in heaps triggers rapid aflatoxin mold contamination.");
                    r.add("CLEAR CHANNELS: Prevent waterlogging around root zones to protect stem stability.");
                    break;
                case "vegetables":
                    r.add("EXPEDITE PICKING: Harvest mature, ready-to-sell vegetables immediately before rains begin.");
                    r.add("STAKE AND RAISE: Stake creeper and fruiting plants to keep fruits off soaked soil.");
                    r.add("DO NOT LEAVE IN FIELD: Move packed vegetable crates to covered collection sheds immediately.");
                    r.add("DRAIN FIELD BEDS: Open drainage trenches to stop collar rot and fruit bursting.");
                    break;
                case "pulses":
                    r.add("PROTECT AGAINST SHATTERING: Avoid harvesting damp pods; wait for dry sunshine if crops are standing.");
                    r.add("ELEVATE HARVESTED LOTS: Keep pods off wet ground using raised tarpaulins or wooden pallets.");
                    r.add("PREVENT SPROUTING: Dampness causes pod-sprouting; inspect stored heaps twice daily.");
                    break;
                default:
                    r.add("POSTPONE FIELD OPERATIONS: Delay cutting standing crops until the rain risk subsides.");
                    r.add("SECURE PROTECTIVE SHEETS: Keep tarpaulins, plastic sheets, and rope ties ready at the threshing floor.");
                    r.add("FIELD DRAINAGE: Clear drainage ditches to prevent prolonged water accumulation.");
                    r.add("PROTECT PRODUCE: Move cut heaps to covered barns or community drying platforms.");
                    break;
            }
        } else if ("MEDIUM".equals(risk)) {
            switch (crop) {
                case "rice":
                case "paddy":
                    r.add("MONITOR 12-HOUR FORECAST: Keep watchful eye on cloud buildup before commencing harvester machinery.");
                    r.add("TARPAULIN READY: Keep waterproof covers ready at the field boundary.");
                    r.add("HARVEST IN PHASES: Harvest manageable plot sections where same-day threshing and shelter are assured.");
                    break;
                case "wheat":
                    r.add("CHECK GRAIN MOISTURE: Only harvest if grain moisture is under 12% and field can be cleared by afternoon.");
                    r.add("SECURE THRESHING YARD: Prepare raised ground and plastic covers for immediate protection.");
                    break;
                case "cotton":
                    r.add("EXPEDITE MATURE BOLLS: Continue picking mature bolls in the morning; store produce under cover.");
                    r.add("INSPECT HUMIDITY: Watch evening dampness to prevent fungus on open bolls.");
                    break;
                case "maize":
                case "corn":
                    r.add("PREPARE VENTILATED CRIBS: Ensure storage space has good airflow in case humidity increases.");
                    r.add("MONITOR WEATHER UPDATES: Postpone large-scale dehusking if clouds darken.");
                    break;
                case "vegetables":
                    r.add("HARVEST READY PRODUCE: Harvest ripe fruits and leafy produce early in the day.");
                    r.add("CLEAN FURROWS: Inspect irrigation furrows to ensure excess rainwater can flow away quickly.");
                    break;
                case "pulses":
                    r.add("PREPARE DRYING SITES: Arrange clean sheets for rapid covering if unexpected showers arrive.");
                    r.add("AVOID DAMP PILING: Do not bundle wet cut plants into dense piles.");
                    break;
                default:
                    r.add("MAINTAIN VIGILANCE: Check local weather advisories every 4 to 6 hours.");
                    r.add("READY PROTECTIVE GEAR: Have tarpaulins on hand to protect cut produce quickly.");
                    r.add("PLAN REVISED TIMINGS: Schedule cutting during the clearest predicted daylight window.");
                    break;
            }
        } else {
            // LOW Risk
            r.add("FAVORABLE CONDITIONS: Current forecast indicates favorable harvesting conditions.");
            r.add("PROCEED CAUTIOUSLY: Continue planned harvest activities while monitoring standard daytime weather.");
            r.add("PROPER DRYING: Use sunny daylight hours for thorough sun drying before bag packaging.");
            r.add("SAFE STORAGE: Store produce in moisture-proof, rodent-safe storage containers or sacks.");
        }
        return r;
    }

    private List<String> getTeluguRecommendations(String crop, String risk) {
        List<String> r = new ArrayList<>();
        if ("HIGH".equals(risk)) {
            r.add("కోత వాయిదా వేయండి: వర్ష సూచన అధికంగా ఉంది. నిలబడి ఉన్న పంట నేలకూలకుండా కాలువలు తీయండి.");
            r.add("టార్పాలిన్ కవర్లు: కోసిన ధాన్యాన్ని వెంటనే ఎత్తైన ప్రదేశాలలో టార్పాలిన్ కవర్లతో కప్పండి.");
            r.add("మురుగునీటి పారుదల: పొలంలో నీరు నిల్వ ఉండకుండా నీటి నిష్క్రమణ మార్గాలను సిద్ధం చేయండి.");
            r.add("తేమ రక్షణ: తేమ వల్ల ధాన్యం రంగు మారకుండా మరియు మొలకెత్తకుండా జాగ్రత్త వహించండి.");
        } else if ("MEDIUM".equals(risk)) {
            r.add("వాతావరణాన్ని గమనించండి: కోతకు ముందు తాజా వాతావరణ సమాచారాన్ని పరిశీలించండి.");
            r.add("టార్పాలిన్ సిద్ధంగా ఉంచండి: ఆకస్మిక చిరుజల్లుల నుంచి కాపాడటానికి ప్లాస్టిక్ కవర్లు సిద్ధం చేసుకోండి.");
            r.add("విడతలవారీగా కోత: ఒకే రోజులో తరలించగల పరిమాణంలో మాత్రమే కోత చేపట్టండి.");
        } else {
            r.add("అనుకూల వాతావరణం: ప్రస్తుత వాతావరణం పంట కోతకు అనుకూలంగా ఉంది.");
            r.add("సూర్యరశ్మిని ఉపయోగించుకోండి: కోసిన ధాన్యాన్ని బాగా ఆరబెట్టి తేమ శాతం తగ్గించండి.");
            r.add("భద్రమైన నిల్వ: శుభ్రమైన గోనె సంచులలో భద్రపరచండి.");
        }
        return r;
    }

    private List<String> getHindiRecommendations(String crop, String risk) {
        List<String> r = new ArrayList<>();
        if ("HIGH".equals(risk)) {
            r.add("कटाई स्थगित करें: तेज बारिश की चेतावनी है। कटी हुई फसल को खुले खेत में न छोड़ें।");
            r.add("तिरपाल से ढकें: खलिहान में रखी फसल को प्लास्टिक शीट या तिरपाल से तुरंत सुरक्षित करें।");
            r.add("जल निकासी: खेत से अतिरिक्त पानी निकालने के लिए नालियों को तुरंत साफ करें।");
            r.add("फफूंद व अंकुरण से बचाव: नमी से अनाज के दाने काले पड़ने या अंकुरित होने का खतरा रहता है।");
        } else if ("MEDIUM".equals(risk)) {
            r.add("मौसम पर नजर रखें: कटाई शुरू करने से पहले मौसम के ताजा अपडेट की जांच करें।");
            r.add("सुरक्षात्मक तिरपाल तैयार रखें: अचानक बूंदाबांदी से बचाव के लिए तिरपाल खेत के पास रखें।");
            r.add("सीमित कटाई करें: केवल उतनी फसल काटें जिसे उसी दिन सुरक्षित स्थान पर पहुंचाया जा सके।");
        } else {
            r.add("अनुकूल मौसम: वर्तमान मौसम पूर्वानुमान फसल कटाई के लिए पूरी तरह अनुकूल है।");
            r.add("धूप का सदुपयोग करें: कटी फसल को अच्छी तरह सुखाकर ही भंडारण करें।");
            r.add("सुरक्षित भंडारण: अनाज को नमी-मुक्त गोदाम या बोरियों में रखें।");
        }
        return r;
    }
}
